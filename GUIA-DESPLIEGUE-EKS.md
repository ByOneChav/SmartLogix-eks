# Guia de Despliegue - SmartLogix en AWS EKS

## Arquitectura General

```
                    INTERNET
                       |
                +--------------+
                |     ALB      |  (Application Load Balancer - creado automaticamente por K8s)
                | URL publica  |
                +------+-------+
                       |
 +---------------------v------------------------------------------+
 |                    VPC  10.0.0.0/16                             |
 |                                                                 |
 |  +------------------+  +------------------+                    |
 |  | Public 1A        |  | Public 1B        |                    |
 |  | 10.0.1.0/24      |  | 10.0.2.0/24      |                    |
 |  | us-east-1a       |  | us-east-1b       |                    |
 |  |                  |  |                  |                    |
 |  | > Load Balancer  |  | > Load Balancer  |                    |
 |  | > NAT Gateway    |  |                  |                    |
 |  +--------+---------+  +------------------+                    |
 |           |                                                     |
 |  +--------v---------+  +------------------+                    |
 |  | Private 1A       |  | Private 1B       |                    |
 |  | 10.0.3.0/24      |  | 10.0.4.0/24      |                    |
 |  | us-east-1a       |  | us-east-1b       |                    |
 |  |                  |  |                  |                    |
 |  | > Nodos EKS      |  | > Nodos EKS      |                    |
 |  | > Pods (msvc)    |  | > Pods (msvc)    |                    |
 |  +------------------+  +------------------+                    |
 +-------------------------------------------------------------+
```

### Flujo de comunicacion interna del cluster

```
Usuario (navegador)
    |
    v
Frontend (nginx:80) --- proxy_pass /api/ --->  Gateway (8080)
                                                    |
                                    +---------------+---------------+
                                    |               |               |
                                    v               v               v
                              Auth (8081)     Pedido (8082)   Inventario (8083)
                                    |               |               |
                                    v               v               v
                              Eureka (8761)   Envio (8084)    Config (8888)
                                                    |
                                                    v
                                              PostgreSQL (5432)
                                              [authdb, pedidodb,
                                               inventariodb, enviodb]
```

### Orden de arranque (dependencias)

```
1. PostgreSQL      --> Base de datos, no depende de nadie
2. Config Server   --> Sirve configuracion a todos los demas
3. Eureka          --> Registro de servicios, depende de Config
4. Auth, Pedido, Inventario, Envio  --> Dependen de Config, Eureka y PostgreSQL
5. Gateway         --> Depende de Eureka para descubrir los servicios
6. Frontend        --> Depende de Gateway para proxy de API
```

---

## Servicios del Proyecto

| Servicio | Puerto | Tipo | Base de Datos | Imagen ECR |
|---|---|---|---|---|
| PostgreSQL | 5432 | Base de datos | authdb, pedidodb, inventariodb, enviodb | postgres:15-alpine (Docker Hub) |
| msvc-config | 8888 | Servidor de configuracion | - | smartlogix/msvc-config |
| msvc-eureka | 8761 | Service Discovery | - | smartlogix/msvc-eureka |
| msvc-auth | 8081 | Autenticacion (JWT) | authdb | smartlogix/msvc-authservice |
| msvc-pedido | 8082 | Gestion de pedidos | pedidodb | smartlogix/msvc-pedido |
| msvc-inventario | 8083 | Gestion de inventario | inventariodb | smartlogix/msvc-inventario |
| msvc-envio | 8084 | Gestion de envios | enviodb | smartlogix/msvc-envio |
| msvc-gateway | 8080 | API Gateway | - | smartlogix/msvc-gateway |
| frontend | 80 | Angular + Nginx | - | smartlogix/frontend |

---

## PASO 0: Crear la Red (VPC + Subnets)

> **Que es una VPC?** Virtual Private Cloud - tu red privada aislada dentro de AWS. Todo lo que creas (EC2, EKS, RDS) vive dentro de una VPC. Tiene un rango de IPs privadas (CIDR block).

### 0.1 Obtener Account ID

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
echo "Account: $ACCOUNT_ID"
```

### 0.2 Crear la VPC

```bash
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=smartlogix-vpc}]' \
  --query 'Vpc.VpcId' --output text)

echo "VPC: $VPC_ID"
```

> **CIDR 10.0.0.0/16** = 65,536 IPs disponibles. Es el rango de red privada mas comun.

### 0.3 Habilitar DNS

```bash
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-support
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames
```

> **Por que?** EKS necesita DNS para que los servicios se descubran entre si por nombre (ej: `msvc-eureka` en vez de una IP). Sin esto, los pods no pueden comunicarse.

### 0.4 Crear Internet Gateway

```bash
IGW_ID=$(aws ec2 create-internet-gateway \
  --tag-specifications 'ResourceType=internet-gateway,Tags=[{Key=Name,Value=smartlogix-igw}]' \
  --query 'InternetGateway.InternetGatewayId' --output text)

echo "IGW: $IGW_ID"

aws ec2 attach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID
```

> **Que es un Internet Gateway?** La puerta de salida a internet desde tu VPC. Sin el, nada dentro de la VPC puede acceder a internet. Es necesario para que el Load Balancer sea accesible publicamente.

### 0.5 Crear Subnets PUBLICAS

```bash
PUB_1A=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.1.0/24 \
  --availability-zone us-east-1a \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=smartlogix-public-1a},{Key=kubernetes.io/role/elb,Value=1}]' \
  --query 'Subnet.SubnetId' --output text)

echo "Public 1A: $PUB_1A"

PUB_1B=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.2.0/24 \
  --availability-zone us-east-1b \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=smartlogix-public-1b},{Key=kubernetes.io/role/elb,Value=1}]' \
  --query 'Subnet.SubnetId' --output text)

echo "Public 1B: $PUB_1B"
```

> **Por que 2 subnets en zonas diferentes (1a y 1b)?** Alta disponibilidad. Si us-east-1a cae, us-east-1b sigue funcionando.
>
> **Tag kubernetes.io/role/elb=1** le dice a EKS: "pon los Load Balancers aqui" (en las publicas, porque el LB necesita ser accesible desde internet).

### 0.6 Habilitar IP publica automatica

```bash
aws ec2 modify-subnet-attribute --subnet-id $PUB_1A --map-public-ip-on-launch
aws ec2 modify-subnet-attribute --subnet-id $PUB_1B --map-public-ip-on-launch
```

### 0.7 Crear Subnets PRIVADAS

```bash
PRIV_1A=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.3.0/24 \
  --availability-zone us-east-1a \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=smartlogix-private-1a},{Key=kubernetes.io/role/internal-elb,Value=1}]' \
  --query 'Subnet.SubnetId' --output text)

echo "Private 1A: $PRIV_1A"

PRIV_1B=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.4.0/24 \
  --availability-zone us-east-1b \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=smartlogix-private-1b},{Key=kubernetes.io/role/internal-elb,Value=1}]' \
  --query 'Subnet.SubnetId' --output text)

echo "Private 1B: $PRIV_1B"
```

> **Tag kubernetes.io/role/internal-elb=1** le dice a EKS que los balanceadores internos van aqui. Los nodos EKS viven en estas subnets privadas, protegidos de internet.

### 0.8 Tabla de rutas PUBLICA

```bash
PUB_RT=$(aws ec2 create-route-table \
  --vpc-id $VPC_ID \
  --tag-specifications 'ResourceType=route-table,Tags=[{Key=Name,Value=smartlogix-public-rt}]' \
  --query 'RouteTable.RouteTableId' --output text)

aws ec2 create-route --route-table-id $PUB_RT --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID

aws ec2 associate-route-table --subnet-id $PUB_1A --route-table-id $PUB_RT
aws ec2 associate-route-table --subnet-id $PUB_1B --route-table-id $PUB_RT
```

> **Ruta 0.0.0.0/0 -> IGW** = "todo el trafico que va a internet sale por el Internet Gateway". Solo las subnets publicas usan esta ruta.

### 0.9 NAT Gateway

```bash
EIP_ALLOC=$(aws ec2 allocate-address --domain vpc --query 'AllocationId' --output text)

NAT_ID=$(aws ec2 create-nat-gateway \
  --subnet-id $PUB_1A \
  --allocation-id $EIP_ALLOC \
  --tag-specifications 'ResourceType=natgateway,Tags=[{Key=Name,Value=smartlogix-nat}]' \
  --query 'NatGateway.NatGatewayId' --output text)

echo "NAT Gateway: $NAT_ID (esperar ~2 min)"
```

> **Que es un NAT Gateway?** Permite que los nodos en subnets privadas salgan a internet (para descargar imagenes Docker de ECR, actualizaciones, etc.) PERO nadie de internet puede entrar por ahi. Es una puerta de un solo sentido.
>
> **Por que en la subnet publica?** El NAT necesita salir a internet, y la ruta a internet esta en las publicas.

Esperar ~2 minutos y verificar:

```bash
aws ec2 describe-nat-gateways --nat-gateway-ids $NAT_ID --query 'NatGateways[0].State' --output text
# Debe decir: available
```

### 0.10 Tabla de rutas PRIVADA

```bash
PRIV_RT=$(aws ec2 create-route-table \
  --vpc-id $VPC_ID \
  --tag-specifications 'ResourceType=route-table,Tags=[{Key=Name,Value=smartlogix-private-rt}]' \
  --query 'RouteTable.RouteTableId' --output text)

aws ec2 create-route --route-table-id $PRIV_RT --destination-cidr-block 0.0.0.0/0 --nat-gateway-id $NAT_ID

aws ec2 associate-route-table --subnet-id $PRIV_1A --route-table-id $PRIV_RT
aws ec2 associate-route-table --subnet-id $PRIV_1B --route-table-id $PRIV_RT
```

> **Ruta 0.0.0.0/0 -> NAT** = "todo lo que va a internet desde las privadas sale por el NAT". Los nodos pueden descargar imagenes Docker, pero nadie de afuera puede entrar directamente.

### 0.11 Security Group

```bash
SG_ID=$(aws ec2 create-security-group \
  --group-name smartlogix-sg \
  --description "SG for SmartLogix EKS" \
  --vpc-id $VPC_ID \
  --query 'GroupId' --output text)

echo "Security Group: $SG_ID"

aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID --protocol -1 --source-group $SG_ID
```

> **Que es un Security Group?** Es un firewall virtual. La regla `--protocol -1 --source-group $SG_ID` dice: "todo lo que tenga este mismo security group puede comunicarse libremente". Asi los microservicios hablan entre si sin restricciones.

### Verificacion

```bash
echo "VPC: $VPC_ID"
echo "Public 1A: $PUB_1A"
echo "Public 1B: $PUB_1B"
echo "Private 1A: $PRIV_1A"
echo "Private 1B: $PRIV_1B"
echo "IGW: $IGW_ID"
echo "NAT: $NAT_ID"
echo "SG: $SG_ID"
```

---

## PASO 1: Crear el Cluster EKS

### 1.1 Obtener los roles IAM

```bash
aws iam list-roles --query "Roles[*].RoleName" --output table
```

> Buscar los roles que contengan `EksClusterRole` y `EksNodeRole`. En AWS Academy los roles son predefinidos, no se pueden crear custom.

```bash
EKS_CLUSTER_ROLE="arn:aws:iam::${ACCOUNT_ID}:role/<NOMBRE_DEL_CLUSTER_ROLE>"
EKS_NODE_ROLE="arn:aws:iam::${ACCOUNT_ID}:role/<NOMBRE_DEL_NODE_ROLE>"
```

> **Por que 2 roles separados? (DEFENSA)**
> - **ClusterRole**: Lo usa el Control Plane (el cerebro de EKS). Necesita permisos para administrar la red, crear load balancers, gestionar los nodos.
> - **NodeRole**: Lo usan las instancias EC2 (los nodos). Necesitan permisos para descargar imagenes de ECR, reportar su estado al control plane, escribir logs.
> - Es el **principio de minimo privilegio**: cada componente solo tiene los permisos que necesita.

### 1.2 Crear el cluster

```bash
aws eks create-cluster \
  --name smartlogix-cluster \
  --role-arn $EKS_CLUSTER_ROLE \
  --resources-vpc-config subnetIds=${PUB_1A},${PUB_1B},${PRIV_1A},${PRIV_1B},securityGroupIds=${SG_ID}
```

> **Que es EKS? (DEFENSA)** Elastic Kubernetes Service - Kubernetes administrado por AWS. AWS se encarga del Control Plane (el cerebro), tu solo configuras los nodos (workers).
>
> Se le pasan las 4 subnets porque el control plane necesita comunicarse con ambas zonas de disponibilidad.

Verificar estado (~10-15 min):

```bash
aws eks describe-cluster --name smartlogix-cluster --query "cluster.status" --output text
# Esperar hasta que diga: ACTIVE
```

### 1.3 Crear el Node Group

```bash
aws eks create-nodegroup \
  --cluster-name smartlogix-cluster \
  --nodegroup-name smartlogix-nodes \
  --node-role $EKS_NODE_ROLE \
  --subnets $PRIV_1A $PRIV_1B \
  --instance-types t3.medium \
  --scaling-config minSize=2,maxSize=4,desiredSize=2 \
  --disk-size 20 \
  --ami-type AL2023_x86_64_STANDARD
```

> **Cada parametro explicado (DEFENSA):**
>
> | Parametro | Valor | Justificacion |
> |---|---|---|
> | --subnets | PRIV_1A PRIV_1B | Nodos en subnets privadas = protegidos de internet |
> | --instance-types | t3.medium | 2 vCPU, 4GB RAM. Cada microservicio Java consume ~400MB |
> | --scaling-config | min=2, max=4, desired=2 | min=2 para tolerancia a fallos. max=4 para escalar bajo carga |
> | --disk-size | 20 | 20GB para imagenes Docker y logs |
> | --ami-type | AL2023_x86_64_STANDARD | Amazon Linux 2023 optimizado para EKS K8s 1.33+ |
>
> **Por que min=2?** Si un nodo muere, los pods se redistribuyen al otro. Con 1 solo nodo, si muere, se cae todo.
>
> **Por que subnets privadas?** Defensa en profundidad. Los nodos no necesitan IP publica. Salen a internet por el NAT Gateway.

Verificar estado (~5 min):

```bash
aws eks describe-nodegroup \
  --cluster-name smartlogix-cluster \
  --nodegroup-name smartlogix-nodes \
  --query "nodegroup.status" --output text
# Esperar hasta que diga: ACTIVE
```

### 1.4 Conectar kubectl

```bash
aws eks update-kubeconfig --name smartlogix-cluster --region us-east-1
```

### 1.5 Verificar

```bash
kubectl get nodes
# Debe mostrar 2 nodos en estado Ready
```

---

## PASO 2: Crear Repositorios ECR

```bash
for SERVICE in msvc-config msvc-eureka msvc-gateway msvc-authservice msvc-pedido msvc-inventario msvc-envio frontend; do
  aws ecr create-repository \
    --repository-name smartlogix/$SERVICE \
    --query 'repository.repositoryUri' --output text
  echo "---"
done
```

> **Que es ECR? (DEFENSA)** Elastic Container Registry - tu Docker Hub privado dentro de AWS. Almacena las imagenes Docker de cada servicio. Cuando K8s necesita levantar un pod, descarga la imagen desde ECR.

---

## PASO 3: Instalar Metrics Server

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

> Necesario para que el HPA (autoscaling) pueda leer metricas de CPU y memoria de los pods.

---

## PASO 4: Crear usuario de prueba

```bash
kubectl run curl-test --rm -it --image=curlimages/curl -- \
  curl -s -X POST http://msvc-auth:8081/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Admin","email":"admin@smartlogix.com","password":"admin123","rol":"ADMIN"}'
```

---

## PASO 5: Verificacion completa

```bash
# Ver todos los pods
kubectl get pods

# Ver todos los servicios (buscar la URL publica del frontend)
kubectl get svc

# Ver HPAs (autoscaling)
kubectl get hpa

# Ver la URL publica del frontend
kubectl get svc frontend -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'

# Ver logs de un servicio especifico
kubectl logs -l app=msvc-gateway --tail=50

# Ver logs de PostgreSQL
kubectl logs -l app=postgres --tail=50

# Descripcion detallada de un pod (util para debuggear)
kubectl describe pod -l app=msvc-auth
```

---

## Pipeline CI/CD (GitHub Actions)

### Flujo completo

```
Developer hace push a GitHub
    |
    v
GitHub Actions se activa
    |
    +-- JOB 1: build-and-push (8 jobs en paralelo)
    |       |
    |       +-- Checkout codigo
    |       +-- Login a AWS (credenciales desde GitHub Secrets)
    |       +-- Login a ECR
    |       +-- Docker build (multi-stage)
    |       +-- Docker push a ECR (tag: latest + commit SHA)
    |
    +-- JOB 2: deploy (espera a que JOB 1 termine)
            |
            +-- Checkout codigo
            +-- Login a AWS
            +-- Configurar kubeconfig (conectar kubectl a EKS)
            +-- Reemplazar <ACCOUNT_ID> en manifiestos
            +-- kubectl apply en ORDEN de dependencias:
            |     PostgreSQL -> Config -> Eureka -> Microservicios -> Gateway -> Frontend
            +-- Aplicar HPAs
            +-- Instalar Metrics Server
            +-- kubectl set image (actualizar imagenes con commit SHA)
            +-- Verificacion final (kubectl get pods/svc/hpa)
```

### Rolling Update con Commit SHA

El pipeline usa `kubectl set image` para actualizar cada deployment con el tag del commit SHA actual:

```yaml
- name: Actualizar imagenes con commit SHA
  run: |
    kubectl set image deployment/msvc-config msvc-config=$ECR_REGISTRY/smartlogix/msvc-config:$GITHUB_SHA
    kubectl set image deployment/msvc-eureka msvc-eureka=$ECR_REGISTRY/smartlogix/msvc-eureka:$GITHUB_SHA
    kubectl set image deployment/msvc-auth msvc-auth=$ECR_REGISTRY/smartlogix/msvc-authservice:$GITHUB_SHA
    kubectl set image deployment/msvc-pedido msvc-pedido=$ECR_REGISTRY/smartlogix/msvc-pedido:$GITHUB_SHA
    kubectl set image deployment/msvc-inventario msvc-inventario=$ECR_REGISTRY/smartlogix/msvc-inventario:$GITHUB_SHA
    kubectl set image deployment/msvc-envio msvc-envio=$ECR_REGISTRY/smartlogix/msvc-envio:$GITHUB_SHA
    kubectl set image deployment/msvc-gateway msvc-gateway=$ECR_REGISTRY/smartlogix/msvc-gateway:$GITHUB_SHA
    kubectl set image deployment/frontend frontend=$ECR_REGISTRY/smartlogix/frontend:$GITHUB_SHA
```

> **(DEFENSA) Por que se usa el commit SHA como tag de imagen?**
>
> Cada imagen se sube a ECR con dos tags: `latest` y el SHA del commit (ej: `a1b2c3d`).
> Cuando `kubectl set image` cambia el tag del deployment, Kubernetes compara:
> - Si el SHA es **diferente** al que tiene el pod actual -> reinicia ese pod con la imagen nueva (rolling update)
> - Si el SHA es **igual** -> no hace nada, el pod sigue corriendo sin interrupcion
>
> Esto significa que **solo se reinician los servicios que realmente cambiaron** en ese commit.
> No se reinicia todo el cluster innecesariamente.
>
> Ademas, si algo falla, se puede hacer rollback a una version anterior:
> ```bash
> kubectl rollout undo deployment/msvc-pedido
> ```
> Esto revierte al tag anterior automaticamente.
>
> **Esto es un Zero Downtime Deployment (Rolling Update):**
> K8s primero crea el pod nuevo, espera a que pase el readinessProbe,
> y solo entonces mata el pod viejo. En ningun momento hay interrupcion del servicio.

### GitHub Secrets necesarios

| Secret | Descripcion | Donde obtenerlo |
|---|---|---|
| AWS_ACCESS_KEY_ID | Access key de AWS | AWS Academy > AWS Details > AWS CLI |
| AWS_SECRET_ACCESS_KEY | Secret key | Mismo lugar |
| AWS_SESSION_TOKEN | Session token (Academy lo requiere) | Mismo lugar |
| AWS_ACCOUNT_ID | Numero de cuenta de 12 digitos | `aws sts get-caller-identity` |

> **IMPORTANTE:** En AWS Academy los tokens se regeneran cada vez que inicias el lab. El dia del examen hay que actualizarlos.

---

## Manifiestos de Kubernetes - Explicacion

### Tipos de recursos utilizados

| Recurso | Que es | Para que lo usamos |
|---|---|---|
| **Deployment** | Define cuantas replicas de un pod crear, que imagen usar, variables de entorno | Cada microservicio tiene un Deployment |
| **Service** | Expone un Deployment con un nombre DNS fijo dentro del cluster | Permite que los servicios se comuniquen por nombre |
| **Secret** | Almacena datos sensibles (passwords, tokens) en base64 | Contraseña de BD, JWT secret |
| **ConfigMap** | Almacena configuracion no sensible | Script SQL de inicializacion, config de nginx |
| **HPA** | Horizontal Pod Autoscaler - escala pods automaticamente basado en metricas | Autoscaling por CPU al 50% |

### Tipos de Service

| Tipo | Acceso | Donde lo usamos |
|---|---|---|
| **ClusterIP** | Solo interno (dentro del cluster) | Todos los microservicios, PostgreSQL |
| **LoadBalancer** | Publico (crea un ALB en AWS) | Solo el Frontend |

> **(DEFENSA)** Solo el frontend esta expuesto a internet. Todo lo demas es acceso interno solamente. Esto es **defensa en profundidad**: solo expones lo minimo necesario.

### ReadinessProbe

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8081
  initialDelaySeconds: 40
  periodSeconds: 10
```

> **(DEFENSA)** K8s hace un GET a `/actuator/health` para saber si el pod esta listo para recibir trafico. `initialDelaySeconds: 40` porque Java/Spring tarda ~30-40 segundos en arrancar. Sin esto, K8s mandaria trafico a un pod que aun esta arrancando.

### Resources (requests y limits)

```yaml
resources:
  requests:
    cpu: 200m
    memory: 256Mi
  limits:
    cpu: 500m
    memory: 512Mi
```

> **(DEFENSA)**
> - `requests` = lo minimo que el pod necesita. K8s usa esto para decidir en que nodo ponerlo.
> - `limits` = lo maximo que puede consumir. Si pasa del limite de memoria, K8s mata el pod.
> - HPA calcula el porcentaje basandose en `requests`. Si pediste 200m de CPU y el pod usa 100m = 50%.

### HPA - Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  scaleTargetRef:
    kind: Deployment
    name: msvc-gateway
  minReplicas: 1
  maxReplicas: 3
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 50
```

> **(DEFENSA)** "Configure el umbral en 50% de CPU porque es el punto optimo: si espero a 80% los pods nuevos no alcanzan a arrancar antes de que el servicio se sature (Java tarda ~30 segundos en iniciar). Con 50% hay margen para que el nuevo pod arranque mientras los existentes siguen respondiendo sin degradacion."

### Perfiles de Spring (k8s)

Se creo un perfil `k8s` para cada servicio en `msvc-config/src/main/resources/configurations/`:

| Archivo | Que cambia respecto a dev/qa |
|---|---|
| msvc-auth-k8s.yml | DB host = `postgres`, Eureka = `msvc-eureka` |
| msvc-pedido-k8s.yml | DB host = `postgres`, Eureka = `msvc-eureka` |
| msvc-inventario-k8s.yml | DB host = `postgres`, Eureka = `msvc-eureka` |
| msvc-envio-k8s.yml | DB host = `postgres`, Eureka = `msvc-eureka` |
| msvc-gateway-k8s.yml | Rutas a servicios por nombre DNS, Eureka = `msvc-eureka` |
| msvc-eureka-k8s.yml | hostname = `msvc-eureka` |

> **(DEFENSA)** "En Kubernetes no usamos IPs porque los pods son efimeros, se crean y destruyen constantemente. En su lugar usamos nombres de Service que K8s resuelve internamente via DNS (CoreDNS). Asi si un pod muere y se recrea con otra IP, el nombre sigue funcionando."

---

## Dockerfiles - Multi-stage Build

```dockerfile
# ETAPA 1: BUILD (compilar)
FROM maven:3.9.6-eclipse-temurin-17 as build    # ~800MB
WORKDIR /app
COPY pom.xml ./parent-pom.xml
RUN mvn install -f parent-pom.xml -N -B
COPY msvc-pedido/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B                 # Cache de dependencias
COPY msvc-pedido/src ./src
RUN mvn clean package -DskipTests -B

# ETAPA 2: RUNTIME (ejecutar)
FROM eclipse-temurin:17-jre-alpine               # ~200MB
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> **(DEFENSA)** Multi-stage build: la imagen final solo tiene el JRE + el .jar (~200MB vs ~800MB). No arrastra Maven, codigo fuente ni dependencias de compilacion.
>
> **Cache de Docker**: se copia el pom.xml ANTES que el codigo fuente. Si el codigo cambia pero las dependencias no, Docker reutiliza la capa de dependencias del cache. Un rebuild tipico tarda ~30 segundos en vez de ~3 minutos.

---

## Conceptos Clave para la Defensa

### Orquestacion de Contenedores

> "La orquestacion es el proceso de automatizar el despliegue, escalado y gestion de contenedores. Kubernetes es el estandar de la industria. Se encarga de: decidir en que nodo corre cada contenedor, reiniciar contenedores que fallan, escalar cuando hay mas carga, y balancear el trafico."

### Cluster EKS

> "Un cluster EKS tiene dos componentes: el Control Plane (administrado por AWS, es el cerebro que toma decisiones) y los Nodos (instancias EC2 donde realmente corren los contenedores). Los nodos se agrupan en Node Groups con autoscaling."

### Tolerancia a Fallos

> "Si un pod muere, K8s automaticamente crea uno nuevo (self-healing). Si un nodo cae, los pods se redistribuyen a los nodos restantes. Por eso tenemos min=2 nodos. El readinessProbe asegura que solo se envia trafico a pods que estan listos."

### Escalabilidad

> "Tenemos dos niveles de autoscaling: a nivel de pods (HPA - escala horizontalmente creando mas replicas cuando el CPU supera 50%) y a nivel de nodos (el Node Group puede escalar de 2 a 4 nodos si no hay espacio para mas pods)."

### Alta Disponibilidad

> "Los nodos estan distribuidos en 2 zonas de disponibilidad (us-east-1a y us-east-1b). Si una zona cae, la otra sigue operando. Las subnets publicas y privadas estan duplicadas en ambas zonas."

### Networking

> "Los microservicios estan en subnets privadas (sin IP publica, sin acceso directo desde internet). Salen a internet por el NAT Gateway. Solo el frontend esta expuesto via LoadBalancer (ALB). La comunicacion interna es por DNS de Kubernetes (CoreDNS)."

### CI/CD

> "El pipeline automatiza completamente el ciclo: un push a GitHub dispara GitHub Actions, que construye las imagenes Docker, las sube a ECR, y las despliega al cluster EKS. Todo sin intervencion manual. Si algo falla, se puede ver en los logs de GitHub Actions."

### Secrets

> "Las credenciales nunca estan en el codigo. En GitHub se guardan como Repository Secrets cifrados (AWS keys). En Kubernetes se crean como objetos Secret que se inyectan a los pods como variables de entorno. Asi cumplimos con el principio de no exponer credenciales."

---

## Troubleshooting Comun

| Problema | Comando para diagnosticar | Solucion comun |
|---|---|---|
| Pod en Pending | `kubectl describe pod <nombre>` | Verificar PVC, recursos, nodos disponibles |
| Pod en CrashLoopBackOff | `kubectl logs <nombre>` | Revisar logs, verificar variables de entorno |
| Pod en 0/1 Running | `kubectl describe pod <nombre>` | ReadinessProbe fallando (403 = Spring Security bloqueando /actuator/health) |
| Service sin EXTERNAL-IP | `kubectl get svc` | Verificar que el tipo sea LoadBalancer, verificar subnets publicas |
| Imagenes no se descargan | `kubectl describe pod <nombre>` | Verificar ECR URI, verificar que el NodeRole tenga permisos ECR |
| Pipeline falla en build | Ver logs de GitHub Actions | Verificar Dockerfile (ej: -DskipTests con S) |
| Pipeline falla en deploy | Ver logs de GitHub Actions | Verificar YAML valido, verificar Secrets en GitHub |

---

## Estructura de Archivos K8s

```
backend/k8s/
  +-- postgres-secret.yaml           # Credenciales de PostgreSQL
  +-- postgres-init-configmap.yaml   # Script SQL para crear las 4 BDs
  +-- postgres-deployment.yaml       # Pod de PostgreSQL
  +-- postgres-service.yaml          # DNS interno: postgres:5432
  +-- app-secret.yaml                # DB_PASSWORD y JWT_SECRET
  +-- config-deployment.yaml         # Config Server
  +-- config-service.yaml            # DNS: msvc-config:8888
  +-- eureka-deployment.yaml         # Service Discovery
  +-- eureka-service.yaml            # DNS: msvc-eureka:8761
  +-- auth-deployment.yaml           # Servicio de autenticacion
  +-- auth-service.yaml              # DNS: msvc-auth:8081
  +-- pedido-deployment.yaml         # Servicio de pedidos
  +-- pedido-service.yaml            # DNS: msvc-pedido:8082
  +-- inventario-deployment.yaml     # Servicio de inventario
  +-- inventario-service.yaml        # DNS: msvc-inventario:8083
  +-- envio-deployment.yaml          # Servicio de envios
  +-- envio-service.yaml             # DNS: msvc-envio:8084
  +-- gateway-deployment.yaml        # API Gateway
  +-- gateway-service.yaml           # DNS: msvc-gateway:8080
  +-- frontend-configmap.yaml        # Config de nginx para K8s
  +-- frontend-deployment.yaml       # Angular + Nginx
  +-- frontend-service.yaml          # DNS: frontend:80 (LoadBalancer)
  +-- hpa-auth.yaml                  # Autoscaling para auth
  +-- hpa-pedido.yaml                # Autoscaling para pedido
  +-- hpa-inventario.yaml            # Autoscaling para inventario
  +-- hpa-envio.yaml                 # Autoscaling para envio
  +-- hpa-gateway.yaml               # Autoscaling para gateway
  +-- hpa-frontend.yaml              # Autoscaling para frontend
```

---

## Observaciones y Decisiones Tecnicas

1. **emptyDir en PostgreSQL**: Se uso `emptyDir` en vez de `PersistentVolumeClaim` porque el EBS CSI Driver requeria permisos IAM que AWS Academy no soporta. En produccion se usaria PVC con EBS para persistencia de datos.

2. **Perfil k8s**: Se creo un perfil Spring `k8s` separado de `dev` y `qa` para no romper los ambientes existentes. Este perfil usa nombres DNS de Kubernetes en vez de IPs.

3. **Frontend con ConfigMap de nginx**: En vez de crear un Dockerfile diferente para K8s, se usa un ConfigMap que sobreescribe el `nginx.conf` al desplegar. Asi el Dockerfile original sirve para cualquier entorno.

4. **apiUrl vacio en Angular**: Con `apiUrl: ''`, las peticiones van al mismo servidor (nginx), que las redirige al gateway internamente. Esto evita problemas de CORS y funciona con cualquier URL publica.

5. **ReadinessProbe + actuator**: Spring Security bloqueaba `/actuator/health` con 403. Se agrego `.requestMatchers("/actuator/**").permitAll()` en los SecurityConfig de auth, pedido y envio.

6. **NAT Gateway**: Necesario para que los nodos en subnets privadas puedan descargar imagenes de ECR. Costo: ~$0.045/hora.
