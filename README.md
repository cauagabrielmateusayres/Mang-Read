# Manga Reader 📚

Gerenciador e leitor de mangás em PDF com persistência local em JSON. O app é totalmente portátil: basta apontar a pasta onde estão seus mangás e seu progresso será salvo automaticamente em um arquivo na própria pasta da biblioteca.

## ✨ Funcionalidades
- **Suporte a Múltiplas Bibliotecas**: Adicione várias pastas de mangás simultaneamente.
- **Scan Inteligente**: Detecta novos capítulos e atualiza capas sem duplicar entradas.
- **Visual Moderno**: Interface em Dark Mode com badges de progresso e favoritos.
- **Troca de Capas**: Escolha qualquer imagem do computador para ser a capa do seu mangá.
- **Leitor Integrado**: Navegação por teclado, zoom com mouse e salvamento automático de progresso.

---

## 🚀 Como Rodar

### Pré-requisitos
- **JDK 21** instalado e configurado no PATH.
- **Maven** instalado.

### Compilação e Execução
```powershell
# 1. Clone o repositório
git clone https://github.com/seu-usuario/manga-reader.git
cd manga-reader

# 2. Compile o projeto
mvn clean install -DskipTests

# 3. Execute o aplicativo
mvn javafx:run -pl manga-reader-app
```

---

## 🗂️ Estrutura de Pastas Recomendada
Para o melhor funcionamento do scan, organize seus mangás da seguinte forma:
```
MinhaBiblioteca/
├── Naruto/
│   ├── capitulo_01.pdf
│   ├── capitulo_02.pdf
│   └── cover.jpg (opcional)
├── One Piece/
│   ├── volume_01.pdf
│   └── volume_02.pdf
```

---

## ⌨️ Atalhos no Leitor

| Tecla | Ação |
|-------|------|
| `→` ou `Espaço` | Próxima página |
| `←` | Página anterior |
| `Page Down` | Próximo capítulo |
| `Page Up` | Capítulo anterior |
| `Ctrl` + `+ / -` | Zoom In / Out |
| `Ctrl` + `Scroll` | Zoom In / Out (Mouse) |

---

## 🖼️ Capas dos Mangás
O app tenta encontrar automaticamente uma capa para o seu mangá.
1. Coloque uma imagem (JPG, PNG ou WEBP) dentro da pasta do mangá.
2. Nomeie-a preferencialmente como `cover.jpg` ou `folder.jpg`.
3. Se não aparecer, você pode clicar com o **botão direito** no card na biblioteca e selecionar **"Mudar Capa"** para escolher uma imagem manualmente.

---

## 🏗️ Arquitetura
O projeto é dividido em módulos Maven para melhor organização:
- `manga-reader-core`: Regras de negócio, modelos e interfaces.
- `manga-reader-persistence`: Implementação de banco de dados baseado em arquivos JSON (`manga-reader-data.json`).
- `manga-reader-app`: Interface gráfica em JavaFX e renderização de PDFs.


