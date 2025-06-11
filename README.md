# Demonstração Prática: Acessando Segredos Armazenados em um Password Vault

## Pré-requisitos

- Docker instalado na máquina
- Terminal/linha de comando
- Conexão com a internet para baixar as imagens Docker
- "bin/sh ou bash"
- Clone o repositorio

## Passo a Passo

### 0. Clone o repositorio
```bash
# Isso sera utilizado no futuro
git clone https://github.com/ifsc-seg-classroom/grupo-8.git
```

### 1. Configurar o ambiente com Docker

Cria um contêiner Docker com o HashiCorp Vault:

```bash
# [opcional] - Para uso fora de "sudo"
dockerd-rootless-setuptool.sh install

# Puxa e executa o Vault em modo desenvolvimento (apenas para demonstração)
docker run --cap-add=IPC_LOCK -d --name=dev-vault -p 8200:8200 hashicorp/vault server -dev
```

### 2. Acessar a interface do Vault

O Vault estará disponível em http://localhost:8200. Você pode ver o token de root (para desenvolvimento) na saída do comando anterior ou com:

```bash
# Copie o token que aparece após `Root Token:`.
docker logs dev-vault | grep "Root Token:"
```
<details>
  <summary>Imagem Guia</summary>
  
  ![image](https://github.com/user-attachments/assets/60337150-92e0-41f7-b734-e1e98daa91ba)
  <br>
  ![image](https://github.com/user-attachments/assets/4a0c74bf-2f65-45eb-afe5-7e45cbb08377)
</details>


### 3. Armazenar uma chave API no Vault

Como admin, entramos com bash no container na CLI do Vault para armazenar nossa chave API:
<br>
**_OBS:_**  Coloque o token obtido acima no primeiro _PLACEHOLDER_ & altere o segundo _PLACEHOLDER_ com uma chave propria.

```bash
# Acessar o shell do contêiner
docker exec -it dev-vault /bin/sh

# Configurar variável de ambiente com o token
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='PLACEHOLDER'

# Armazenar uma chave API secreta (ex: fncx puof jioq eiss")
vault kv put secret/api-keys/producao key="fncx puof jioq eiss"
```
<details>
  <summary>Imagem Guia</summary>
  
  ![image](https://github.com/user-attachments/assets/94c837a6-8b4a-42b8-b6eb-ebcb7b34d575)
</details>

### 4. Criar uma política de acesso

Ainda dentro do contêiner, vamos criar uma política que permite apenas leitura do segredo:

```bash
# Criar arquivo de política
cat > api-policy.hcl <<EOF
path "secret/data/api-keys/producao" {
  capabilities = ["read"]
}
EOF

# Aplicar a política
vault policy write api-reader api-policy.hcl
```

<details>
  <summary>Imagem Guia</summary>

  ![image](https://github.com/user-attachments/assets/030a5db8-7560-4029-bcd1-af9fa0732686)
</details>

### 5. Gerar um token com permissões limitadas

```bash
# Criar token com a política api-reader
vault token create -policy="api-reader" -ttl=24h
```

<details>
  <summary>Imagem Guia</summary>
  
  ![image](https://github.com/user-attachments/assets/591831b0-2370-4ad0-a0ec-b3a3efc5e7bd)
  <br>
  ![image](https://github.com/user-attachments/assets/8cd48a49-38b5-46fe-8579-56ed47311bec)
</details>

<br>

**_OBS:_** Anote o token gerado - este será usado pela aplicação.

### 6. Simular uma aplicação acessando o segredo

Vamos inciar o app do repositorio clonado, visite http://localhost:8080

```bash
# Entre no repositorio
cd grupo-8
# Coloque o token no application.yml, salva e fecha.
gedit src/main/resources/application.yml
# Inicia app
./gradlew bootRun
```

<details>
  <summary>Imagem Guia</summary>
    
  ![image](https://github.com/user-attachments/assets/43352e16-c0db-49c4-99a0-a9145e986317)
</details>

<details>
  <summary>Opcao 2 - CLI</summary>
    
```bash
# Fora do container, no host Linux: OBS, se nao tiver "JQ", retire o "| jq" e use algo como: https://jsonformatter.curiousconcept.com
curl -s --header "X-Vault-Token: PLACEHOLDER" http://localhost:8200/v1/secret/data/api-keys/producao | jq
```

  ![image](https://github.com/user-attachments/assets/c942c189-83e8-4c3a-a667-9bcf0bbe8cfb)
</details>

### Extra
Utilizar a UI para demonstrar de melhor forma. Alterando os metadados obtidos & resultado. Com Rastreamento, gerenciamento centralizado e controle.
<details>
  <summary>Imagem Guia</summary>
  Secret Engines -> Secret -> api-keys -> producao -> metadata -> edit <br>
  E use o commando acima denovo quanto terminar.
  
  ![image](https://github.com/user-attachments/assets/1b4a6626-e66f-426e-bcfd-a47f684e9b7d)
  <br>
  ![image](https://github.com/user-attachments/assets/e6006e6e-ca35-45c5-883c-c9a37a50695e)
</details>

### Referencias
- Deepseek - Comandos para rodar em container, modo [dev] e formatação e erros de português.
- hashicorp/vault - Imagem docker com configuração inicial - https://hub.docker.com/r/hashicorp/vault
- Hashicorp Vault - Documentação principal - https://developer.hashicorp.com/vault
