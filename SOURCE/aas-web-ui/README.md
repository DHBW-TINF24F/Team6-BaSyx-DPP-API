# default

## Project setup

```
# yarn
yarn

# npm
npm install

# pnpm
pnpm install
```

### Compiles and hot-reloads for development

```
# yarn
yarn dev

# npm
npm run dev

# pnpm
pnpm dev
```

### Compiles and minifies for production

```
# yarn
yarn build

# npm
npm run build

# pnpm
pnpm build
```

### Check for linting errors

```
# yarn
yarn lint:check

# npm
npm run lint:check

# pnpm
pnpm lint:check
```

### Fix linting errors

```
# yarn
yarn lint:fix

# npm
npm run lint:fix

# pnpm
pnpm lint:fix
```

### Run the prepared STR integration tests in WSL

The prepared system and integration tests referenced by the STR are located in `tests/integration`.

For a reproducible WSL setup, use Node.js 24 and Yarn via Corepack:

```
corepack enable
yarn install
yarn test:integration
yarn test:coverage
```

### Customize configuration

See [Configuration Reference](https://vitejs.dev/config/).
