import React, { Suspense } from 'react'
import { Routes, Route } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import Navbar from './layout/Navbar'
import { LoadingSpinner, Background } from './shared/ui'

// Lazy load pages for better performance
const HomePage = React.lazy(() => import('./features/dashboard/components/HomePage'))
const VisionPage = React.lazy(() => import('./features/nutrition/components/VisionPage'))
const NutritionPage = React.lazy(() => import('./features/nutrition/components/NutritionPage'))
const AIChat = React.lazy(() => import('./features/workout/components/AIChat'))
const StatsPage = React.lazy(() => import('./features/profile/components/StatsPage'))

// Error Boundary Component
class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error?: Error }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error }
  }

  override componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('App Error:', error, errorInfo)
  }

  override render() {
    if (this.state.hasError) {
      return (
        <motion.div 
          className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-500 to-orange-500"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
        >
          <div className="text-center text-white p-8">
            <h1 className="text-4xl font-bold mb-4">Что-то пошло не так</h1>
            <p className="text-xl mb-6">Пожалуйста, перезагрузите страницу</p>
            <button 
              onClick={() => window.location.reload()}
              className="bg-white text-purple-600 px-6 py-3 rounded-lg font-bold hover:bg-gray-100 transition-colors"
            >
              Перезагрузить
            </button>
          </div>
        </motion.div>
      )
    }

    return this.props.children
  }
}

// Page transition variants
const pageVariants = {
  initial: {
    opacity: 0,
    scale: 0.98,
    y: 20
  },
  in: {
    opacity: 1,
    scale: 1,
    y: 0
  },
  out: {
    opacity: 0,
    scale: 1.02,
    y: -20
  }
}

const pageTransition = {
  duration: 0.4
}

function App() {
  return (
    <ErrorBoundary>
      <Background>
        <Navbar />
        
        <main className="pt-16"> {/* Account for fixed navbar */}
          <AnimatePresence mode="wait">
            <Suspense 
              fallback={
                <motion.div 
                  className="flex items-center justify-center min-h-[80vh]"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                >
                  <LoadingSpinner size="lg" />
                </motion.div>
              }
            >
              <Routes>
                <Route path="/" element={
                  <motion.div
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <HomePage />
                  </motion.div>
                } />
                
                <Route path="/vision" element={
                  <motion.div
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <VisionPage />
                  </motion.div>
                } />

                <Route path="/nutrition" element={
                  <motion.div
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <NutritionPage />
                  </motion.div>
                } />
                
                <Route path="/chat" element={
                  <motion.div
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <AIChat />
                  </motion.div>
                } />
                
                <Route path="/stats" element={
                  <motion.div
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <StatsPage />
                  </motion.div>
                } />

                {/* 404 Page */}
                <Route path="*" element={
                  <motion.div
                    className="min-h-[80vh] flex items-center justify-center"
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <div className="text-center">
                      <h1 className="text-6xl font-bold text-gradient-primary mb-4">404</h1>
                      <p className="text-xl text-neutral-600 dark:text-neutral-400 mb-6">
                        Страница не найдена
                      </p>
                      <motion.button
                        onClick={() => window.history.back()}
                        className="bg-gradient-to-r from-purple-500 to-orange-500 text-white px-8 py-3 rounded-lg font-bold hover:from-purple-600 hover:to-orange-600 transition-all transform hover:scale-105"
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                      >
                        Вернуться назад
                      </motion.button>
                    </div>
                  </motion.div>
                } />
                          </Routes>
          </Suspense>
        </AnimatePresence>
      </main>
    </Background>
    </ErrorBoundary>
  )
}

export default App
