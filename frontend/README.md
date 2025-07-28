# mvpFitness Frontend

Премиальный фронтенд на React 19 + TypeScript 5.5 с современными анимациями и дизайном.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default tseslint.config([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      ...tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      ...tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      ...tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default tseslint.config([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

## 🎨 Background Component

Универсальный Background компонент с живыми градиентами и анимированными формами.

### Использование

```tsx
import { Background } from './shared/ui'

// Базовое использование
<Background>
  <YourContent />
</Background>

// С настройками
<Background 
  variant="animated"
  animate={true}
  opacity={0.9}
  overlayChildren={true}
>
  <YourContent />
</Background>
```

### Варианты (Variants)

- **default** - Адаптивный градиент (light/dark режимы)
- **dark** - Темный режим с фиолетовыми тонами
- **minimal** - Минимальный светлый градиент
- **animated** - Анимированный градиент primary → secondary

### Пропсы

| Prop | Type | Default | Описание |
|------|------|---------|----------|
| `variant` | string | 'default' | Вариант фонового стиля |
| `opacity` | number | 1 | Прозрачность фона |
| `animate` | boolean | true | Включить анимации |
| `overlayChildren` | boolean | false | Glassmorphism для детей |
| `className` | string | - | Дополнительные CSS классы |

### Особенности

- 🎨 **Живые градиенты** - плавные переходы цветов
- 🌊 **Анимированные орбы** - floating элементы
- 🌓 **Dark Mode** - автоматическая адаптация
- 📱 **Адаптивность** - корректно на всех устройствах
- ⚡ **Performance** - 60fps анимации
- 🎭 **Glassmorphism** - современные эффекты
