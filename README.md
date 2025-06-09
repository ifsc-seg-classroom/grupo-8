# Demonstração Prática: Acessando Segredos Armazenados em um Password Vault

## Introdução

Nesta demonstração, mostrarei como uma aplicação pode acessar com segurança um segredo armazenado em um password vault, simulando um cenário real onde uma aplicação precisa acessar uma chave API sem expô-la diretamente aos desenvolvedores ou usuários finais. Utilizaremos o HashiCorp Vault, uma solução popular para gerenciamento de segredos, e Docker para criar um ambiente isolado sem necessidade de privilégios administrativos no sistema principal.

## Pré-requisitos

- Docker instalado na máquina
- Terminal/linha de comando
- Conexão com a internet para baixar as imagens Docker
- "bin/sh ou bash"

## Passo a Passo

### 1. Configurar o ambiente com Docker

Primeiro, vamos criar um contêiner Docker com o HashiCorp Vault:

```bash
# [opcional] - Para uso fora de "sudo"
dockerd-rootless-setuptool.sh install

# Baixar a imagem oficial do Vault
docker pull hashicorp/vault

# Executar o Vault em modo desenvolvimento (apenas para demonstração)
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

Vamos usar a CLI do Vault dentro do contêiner para armazenar nossa chave API:

```bash
# Acessar o shell do contêiner
docker exec -it dev-vault /bin/sh

# Configurar variável de ambiente com o token
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='PLACEHOLDER'

# Armazenar uma chave API secreta (ex: a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
vault kv put secret/api-keys/producao key="a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6"
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

Anote o token gerado - este será usado pela aplicação.

### 6. Simular uma aplicação acessando o segredo

Vamos utilizar o curl para simular um request de outra aplicação.

```bash
# Fora do container, no host Linux: OBS, se nao tiver "JQ", retire o "| jq" e use algo como: https://jsonformatter.curiousconcept.com
curl -s --header "X-Vault-Token: PLACEHOLDER" http://localhost:8200/v1/secret/data/api-keys/producao | jq
```
<details>
  <summary>Imagem Guia</summary>

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


## Síntese dos Resultados

Nesta demonstração conseguimos:

1. Configurar rapidamente um ambiente de Vault usando Docker, sem necessidade de privilégios administrativos
2. Armazenar com segurança uma chave API no Vault
3. Criar políticas granulares de acesso
4. Gerar tokens com permissões específicas (princípio do menor privilégio)
5. Simular como uma aplicação pode acessar segredos sem tê-los hardcoded no código

Os principais benefícios observados:

- Segurança: A chave API nunca é exposta diretamente no código ou para desenvolvedores
- Rastreabilidade: O Vault mantém logs de quem acessou qual segredo e quando
- Gerenciamento centralizado: As chaves podem ser rotacionadas sem alterar o código da aplicação
- Controle de acesso: Políticas granulares limitam o acesso apenas ao necessário

Esta abordagem é superior à prática comum de armazenar segredos em variáveis de ambiente ou arquivos de configuração, pois oferece maior segurança e controle.
