# Manga Reader 📚

Um leitor de mangás moderno, rápido e modular, desenvolvido em **JavaFX 21**. Este projeto foi projetado para oferecer uma experiência de leitura fluida, com foco em portabilidade e performance, permitindo que você gerencie sua biblioteca de PDFs com facilidade.

> [!IMPORTANT]
> **Projeto Desenvolvido com Inteligência Artificial** 🤖
> Este software foi inteiramente desenvolvido, refatorado e otimizado através de uma parceria entre o desenvolvedor e a IA **Antigravity (Google DeepMind)**. O código segue padrões modernos de engenharia, demonstrando o potencial da colaboração humano-IA no desenvolvimento de software de alta performance.

---

## ✨ Funcionalidades Principais

- **Leitura Dual Mode**:
  - **Modo Paginação**: Navegação clássica página a página.
  - **Modo Rolo (Vertical)**: Leitura contínua com **Virtual Scrolling** (Lazy Loading), carregando apenas o que você está vendo para economizar memória.
- **Gerenciamento de Zoom**:
  - Zoom dinâmico com `Ctrl + Scroll` do mouse.
  - Persistência automática: o nível de zoom é mantido ao trocar de páginas.
  - Exibição da porcentagem de ampliação em tempo real.
- **Suporte a WebP**: Renderização nativa de capas e imagens WebP via ImageIO.
- **Arquitetura Portátil**:
  - Não utiliza SQL externo por padrão; os dados são salvos em um arquivo `manga-reader-data.json` dentro da sua própria biblioteca.
  - Leve o seu HD externo com seus mangás e o progresso irá junto!
- **Scan Inteligente**: Reconhecimento automático de volumes, capítulos e capas (`cover.jpg`, `folder.jpg`, etc).

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java 21 (JDK 21)
- **Interface**: JavaFX 21
- **Processamento de PDF**: Apache PDFBox 3.0
- **Persistência**: GSON (JSON-based database)
- **Gerenciamento de Dependências**: Maven (Multi-module)

---

## 🚀 Como Executar

### Para Usuários (Executável)
Basta baixar a versão mais recente na aba [Releases](https://github.com/cauagabrielmateusayres/Mang-Read/releases), extrair o ZIP e rodar o `MangaReader.exe`.

### Para Desenvolvedores (Código Fonte)
```powershell
# 1. Clone o repositório
git clone https://github.com/cauagabrielmateusayres/Mang-Read.git
cd Mang-Read

# 2. Compile o projeto
mvn clean install -DskipTests

# 3. Execute o aplicativo
mvn javafx:run -pl manga-reader-app
```

---

## ⌨️ Atalhos de Teclado

| Tecla | Ação |
|-------|------|
| `→` / `Espaço` | Próxima Página |
| `←` | Página Anterior |
| `Page Down` | Próximo Capítulo |
| `Page Up` | Capítulo Anterior |
| `Ctrl + Scroll` | Zoom In/Out |
| `F` | Alternar Modo Favorito |

---

## 🏗️ Estrutura do Projeto

O projeto utiliza uma arquitetura modular Maven para separação de responsabilidades:
- `manga-reader-core`: Contém as regras de negócio, modelos de dados e definições de serviços.
- `manga-reader-persistence`: Camada de acesso a dados usando repositórios JSON.
- `manga-reader-app`: O "coração" da aplicação UI, gerenciando a renderização de PDFs e eventos de usuário.

---

## 🤝 Contribuições

Este é um projeto de código aberto. Sinta-se à vontade para abrir **Issues** ou enviar **Pull Requests**!

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---
*Desenvolvido com ❤️ e ⚡ por KablshE & Antigravity AI.*
