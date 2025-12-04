# 🛒 MyCart

## 📱 Descrição

O **MyCart** é um aplicativo Android nativo desenvolvido em Kotlin com o objetivo de facilitar a criação e o gerenciamento de listas de compras de forma prática e intuitiva.

O app permite que o usuário se cadastre, faça login, crie listas personalizadas com imagens, adicione itens com informações detalhadas (quantidade, unidade e categoria), marque produtos já comprados e sincronize todos os dados em tempo real através da nuvem.

O projeto foi desenvolvido como parte da disciplina **Programação Mobile I**, com foco na aplicação dos conceitos de **MVVM**, **Coroutines**, **Flow**, **Firebase**, **ViewBinding**, **Material Design** e **RecyclerView**.

---

## ⚙️ Funcionalidades Principais

### 🔐 RF001 - Autenticação de Usuário
- Tela de login com validação de e-mail e senha
- **Recuperação de senha** via e-mail (Firebase Auth)
- Autenticação real usando **Firebase Authentication**
- Função de logout com redirecionamento para tela inicial
- Indicadores de carregamento durante operações assíncronas

### 👤 RF002 - Cadastro de Usuário
- Cadastro de novos usuários com dados persistidos no **Firebase Firestore**
- Campos: Nome, E-mail, Senha e Confirmação de Senha
- Validação de formato de e-mail, força de senha (mínimo 6 caracteres) e correspondência de senhas
- Feedback visual durante o processo de cadastro

### 🧾 RF003 - Gestão de Listas de Compras
- Criação, edição e exclusão de listas de compras
- Cada lista possui título e **imagem personalizada opcional**
- Imagens armazenadas localmente com gerenciamento automático
- Exibição das listas em **RecyclerView**, ordenadas alfabeticamente
- **Sincronização em tempo real** com Firebase Firestore
- Remoção de listas exclui automaticamente os itens associados e imagens
- Sistema de busca para filtrar listas por nome

### 🛍️ RF004 - Gestão de Itens da Lista
- Criação, edição e exclusão de itens dentro das listas
- Cada item contém: nome, quantidade, unidade e categoria
- **Unidades disponíveis**: UN (Unidade), KG (Quilograma), L (Litro), G (Grama), ML (Mililitro)
- **Categorias**: Alimentos, Bebidas, Higiene, Limpeza, Outros
- Itens agrupados por categoria e ordenados alfabeticamente
- Possibilidade de marcar itens como comprados
- Itens comprados listados separadamente dos não comprados
- **Atualização em tempo real** de todos os itens

### 🔎 RF005 - Sistema de Busca
- Busca eficiente para filtrar listas por nome
- Busca de itens dentro das listas por nome
- Atualização instantânea dos resultados conforme digitação

---

## 🧩 Tecnologias Utilizadas

| Tecnologia | Descrição |
|------------|-----------|
| **Kotlin** | Linguagem principal para desenvolvimento Android |
| **Android Studio** | IDE utilizada para desenvolvimento e testes |
| **MVVM** | Padrão arquitetural (Model-View-ViewModel) |
| **Coroutines** | Programação assíncrona moderna |
| **Flow** | Streams de dados reativos para sincronização em tempo real |
| **Firebase Authentication** | Autenticação de usuários com recuperação de senha |
| **Firebase Firestore** | Banco de dados NoSQL em tempo real na nuvem |
| **ViewBinding** | Acesso seguro e eficiente aos elementos de interface |
| **RecyclerView** | Exibição dinâmica e performática de listas |
| **Material Design 3** | Padrão de design moderno e responsivo |
| **Glide** | Carregamento e cache eficiente de imagens |
| **Repository Pattern** | Abstração da camada de dados |
| **LiveData** | Observação de dados com lifecycle awareness |

---

## 📂 Arquitetura do Projeto

O projeto segue o padrão **MVVM (Model-View-ViewModel)** com as seguintes camadas:

```
app/
├── data/
│   ├── model/           # Modelos de dados (Usuario, ListaCompra, Item)
│   └── repo/            # Repositories (AuthRepository, ListRepository)
│       └── api/         # Interfaces dos repositories
├── domain/              # Regras de negócio e validações
├── ui/                  # Activities e Adapters
│   ├── login/          # Tela de Login
│   ├── cadastro/       # Tela de Cadastro
│   ├── listas/         # Gestão de Listas
│   └── itens/          # Gestão de Itens
├── viewmodel/          # ViewModels (LoginViewModel, RegisterViewModel, etc)
├── util/               # Utilitários (Result, ImageManager, Extensions)
└── di/                 # ServiceLocator para injeção de dependências
```

### Fluxo de Dados
1. **View (Activity)** → Observa LiveData do ViewModel
2. **ViewModel** → Processa lógica e chama Repository
3. **Repository** → Comunica com Firebase (Firestore/Auth)
4. **Firebase** → Retorna dados via Flow (tempo real) ou suspend functions
5. **Result** → Encapsula sucesso, erro ou loading
6. **LiveData** → Notifica a View das mudanças

---

## 🔥 Integração Firebase

### Configuração
- `google-services.json` configurado no projeto
- Firebase Authentication habilitado
- Firebase Firestore com as seguintes coleções:
  - **users**: Dados dos usuários (uid, nome, email, criadoEm)
  - **listas**: Listas de compras (id, userId, titulo, imagemPath, criadaEm, atualizadaEm)
  - **listas/{listaId}/itens**: Itens de cada lista (subcoleção)

### Sincronização em Tempo Real
- Uso de **SnapshotListeners** do Firestore
- Implementação com **Kotlin Flow** e **callbackFlow**
- Atualizações instantâneas em todos os dispositivos logados
- Sem necessidade de recarregar manualmente

---

## 🎨 Interface do Usuário

- Design seguindo **Material Design 3**
- Temas personalizados com cores consistentes
- **Animações** suaves e transições fluidas
- **Feedback visual** em todas as operações (loading, sucesso, erro)
- **Dialogs** para confirmações e recuperação de senha
- Layout responsivo e adaptável

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Android Studio Arctic Fox ou superior
- JDK 11 ou superior
- Dispositivo Android ou Emulador (API 21+)
- Conta no Firebase e arquivo `google-services.json` configurado

### Passos
1. Clone o repositório
2. Abra o projeto no Android Studio
3. Adicione seu arquivo `google-services.json` na pasta `app/`
4. Configure o Firebase Console (Authentication e Firestore)
5. Sincronize o Gradle
6. Execute o app em um dispositivo ou emulador

---

## 👥 Desenvolvedores

- **Vitor Mussi Dalpino**
- **Matheus Da Silva Correa**
- **Lucas Queiroz**

---

## 📋 Melhorias Implementadas (Parte 2)

### ✅ Concluído
- ✅ Integração completa com Firebase Authentication
- ✅ Integração completa com Firebase Firestore
- ✅ Sincronização em tempo real de listas e itens
- ✅ Sistema de recuperação de senha
- ✅ Arquitetura MVVM com Coroutines e Flow
- ✅ Repository Pattern com interfaces
- ✅ Gerenciamento de imagens otimizado
- ✅ Tratamento de erros robusto
- ✅ Loading states em todas as operações
- ✅ Validações de formulários aprimoradas
- ✅ Código limpo e organizado

---

## 🏁 Status do Projeto

✅ **Concluído** — Versão funcional com arquitetura MVVM, Firebase integrado, sincronização em tempo real e pronto para uso em produção.

---

## 📝 Licença

Este projeto foi desenvolvido para fins acadêmicos na disciplina de Programação Mobile I.

