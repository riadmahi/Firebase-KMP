# Homebrew Tap pour KFire

Ce dossier contient les fichiers pour créer le tap Homebrew `riadmahi/kfire`.

## Setup

### 1. Créer le repo `homebrew-kfire`

Crée un nouveau repo GitHub : `riadmahi/homebrew-kfire`

### 2. Copier les fichiers

```bash
# Copier la formula
cp Formula/kfire.rb <path-to-homebrew-kfire>/Formula/

# Copier le workflow
cp workflows/update-formula.yml <path-to-homebrew-kfire>/.github/workflows/
```

### 3. Créer un Personal Access Token

1. Va sur GitHub → Settings → Developer settings → Personal access tokens
2. Crée un token avec les permissions `repo` et `workflow`
3. Ajoute le token comme secret `HOMEBREW_TAP_TOKEN` dans le repo `kfire`

### 4. Release

```bash
# Créer un tag
git tag v1.0.0
git push origin v1.0.0
```

Le workflow va automatiquement :
1. Builder la CLI
2. Créer une release GitHub
3. Mettre à jour la formula dans `homebrew-kfire`

## Installation

```bash
brew tap riadmahi/kfire
brew install kfire
```
