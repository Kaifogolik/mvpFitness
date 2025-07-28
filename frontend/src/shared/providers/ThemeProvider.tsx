import React, { createContext, useContext, useEffect, useState } from 'react'

type Theme = 'light' | 'dark' | 'system'

interface ThemeContextType {
  theme: Theme
  resolvedTheme: 'light' | 'dark'
  setTheme: (theme: Theme) => void
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined)

export const useTheme = () => {
  const context = useContext(ThemeContext)
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider')
  }
  return context
}

interface ThemeProviderProps {
  children: React.ReactNode
  defaultTheme?: Theme
  enableSystem?: boolean
  attribute?: string
  storageKey?: string
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({
  children,
  defaultTheme = 'system',
  enableSystem = true,
  attribute = 'class',
  storageKey = 'mvp-fitness-theme'
}) => {
  const [theme, setThemeState] = useState<Theme>(() => {
    // Check localStorage on initialization
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem(storageKey)
      if (stored && ['light', 'dark', 'system'].includes(stored)) {
        return stored as Theme
      }
    }
    return defaultTheme
  })

  const [resolvedTheme, setResolvedTheme] = useState<'light' | 'dark'>(() => {
    if (theme === 'system' && enableSystem && typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    return theme === 'dark' ? 'dark' : 'light'
  })

  // Apply theme to document
  const applyTheme = (newTheme: 'light' | 'dark') => {
    const root = window.document.documentElement
    
    root.classList.remove('light', 'dark')
    
    if (attribute === 'class') {
      root.classList.add(newTheme)
    } else {
      root.setAttribute(attribute, newTheme)
    }
    
    // Also set data attribute for CSS variables
    root.setAttribute('data-theme', newTheme)
  }

  // Watch for system theme changes
  useEffect(() => {
    if (!enableSystem) return

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    const handleChange = (e: MediaQueryListEvent) => {
      if (theme === 'system') {
        const newResolvedTheme = e.matches ? 'dark' : 'light'
        setResolvedTheme(newResolvedTheme)
        applyTheme(newResolvedTheme)
      }
    }

    mediaQuery.addEventListener('change', handleChange)
    
    return () => mediaQuery.removeEventListener('change', handleChange)
  }, [theme, enableSystem])

  // Apply theme when it changes
  useEffect(() => {
    let newResolvedTheme: 'light' | 'dark'
    
    if (theme === 'system' && enableSystem) {
      newResolvedTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    } else {
      newResolvedTheme = theme === 'dark' ? 'dark' : 'light'
    }
    
    setResolvedTheme(newResolvedTheme)
    applyTheme(newResolvedTheme)
    
    // Save to localStorage
    localStorage.setItem(storageKey, theme)
  }, [theme, enableSystem, storageKey])

  const setTheme = (newTheme: Theme) => {
    setThemeState(newTheme)
  }

  const toggleTheme = () => {
    if (theme === 'system') {
      // If currently system, toggle to opposite of current resolved theme
      setTheme(resolvedTheme === 'dark' ? 'light' : 'dark')
    } else {
      // If currently light/dark, toggle to opposite
      setTheme(theme === 'dark' ? 'light' : 'dark')
    }
  }

  const value: ThemeContextType = {
    theme,
    resolvedTheme,
    setTheme,
    toggleTheme
  }

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  )
}

// Компонент переключателя темы
import { Monitor, Moon, Sun } from 'lucide-react'
import { Button } from '../ui/Button'
import { motion, AnimatePresence } from 'framer-motion'

export const ThemeToggle: React.FC<{ className?: string }> = ({ className }) => {
  const { theme, setTheme, resolvedTheme } = useTheme()
  const [isOpen, setIsOpen] = useState(false)

  const themes: Array<{ value: Theme; label: string; icon: React.ReactNode }> = [
    { value: 'light', label: 'Светлая', icon: <Sun className="w-4 h-4" /> },
    { value: 'dark', label: 'Тёмная', icon: <Moon className="w-4 h-4" /> },
    { value: 'system', label: 'Системная', icon: <Monitor className="w-4 h-4" /> }
  ]

  const currentTheme = themes.find(t => t.value === theme)

  return (
    <div className={`relative ${className}`}>
      <Button
        variant="ghost"
        size="sm"
        onClick={() => setIsOpen(!isOpen)}
        className="relative"
      >
        <motion.div
          key={resolvedTheme}
          initial={{ rotate: -180, opacity: 0 }}
          animate={{ rotate: 0, opacity: 1 }}
          exit={{ rotate: 180, opacity: 0 }}
          transition={{ duration: 0.3 }}
        >
          {currentTheme?.icon}
        </motion.div>
      </Button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: -10 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: -10 }}
            transition={{ duration: 0.2 }}
            className="absolute right-0 top-full mt-2 w-48 glass rounded-xl border border-white/20 shadow-xl z-50"
            onBlur={() => setIsOpen(false)}
          >
            <div className="p-2">
              {themes.map((themeOption) => (
                <motion.button
                  key={themeOption.value}
                  className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${
                    theme === themeOption.value
                      ? 'bg-primary-100 text-primary-900 dark:bg-primary-800 dark:text-primary-100'
                      : 'hover:bg-neutral-100 dark:hover:bg-neutral-800'
                  }`}
                  onClick={() => {
                    setTheme(themeOption.value)
                    setIsOpen(false)
                  }}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                >
                  {themeOption.icon}
                  <span>{themeOption.label}</span>
                  {theme === themeOption.value && (
                    <motion.div
                      layoutId="activeTheme"
                      className="ml-auto w-2 h-2 bg-primary-500 rounded-full"
                      initial={false}
                      transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
                    />
                  )}
                </motion.button>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
} 