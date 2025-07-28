import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Home, Camera, Apple, MessageCircle, BarChart3, Zap, Menu, X } from 'lucide-react'
import { Button } from '../shared/ui/Button'
import { cn } from '../shared/utils/cn'

const Navbar: React.FC = () => {
  const location = useLocation()
  const [isMobileMenuOpen, setIsMobileMenuOpen] = React.useState(false)

  const navItems = [
    { path: '/', label: 'Главная', icon: Home },
    { path: '/vision', label: 'Анализ фото', icon: Camera },
    { path: '/nutrition', label: 'Питание', icon: Apple },
    { path: '/chat', label: 'AI Чат', icon: MessageCircle },
    { path: '/stats', label: 'Статистика', icon: BarChart3 },
  ]

  const toggleMobileMenu = () => setIsMobileMenuOpen(!isMobileMenuOpen)

  return (
    <>
      <nav className="sticky top-0 z-50 glass border-b border-white/10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <Link to="/" className="flex items-center space-x-3">
              <motion.div
                whileHover={{ scale: 1.05, rotate: 5 }}
                whileTap={{ scale: 0.95 }}
                className="relative"
              >
                <div className="w-10 h-10 gradient-primary rounded-xl flex items-center justify-center shadow-xl">
                  <Zap className="w-6 h-6 text-white" />
                </div>
                <motion.div
                  className="absolute -top-1 -right-1 w-3 h-3 bg-orange-500 rounded-full"
                  animate={{ scale: [1, 1.2, 1] }}
                  transition={{ duration: 2, repeat: Infinity }}
                />
              </motion.div>
              <div className="hidden sm:block">
                <span className="text-xl font-bold text-gradient-primary">mvpFitness</span>
                <div className="text-xs text-gray-500 font-medium -mt-1">Premium AI Coach</div>
              </div>
            </Link>

            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center space-x-1">
              {navItems.map((item) => {
                const Icon = item.icon
                const isActive = location.pathname === item.path
                
                return (
                  <Link key={item.path} to={item.path}>
                    <motion.div
                      className={cn(
                        'relative px-4 py-2 rounded-xl transition-all duration-300',
                        isActive 
                          ? 'text-white' 
                          : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
                      )}
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                    >
                      <div className="flex items-center space-x-2 relative z-10">
                        <Icon className="w-4 h-4" />
                        <span className="font-medium">{item.label}</span>
                      </div>
                      {isActive && (
                        <motion.div
                          layoutId="activeTab"
                          className="absolute inset-0 gradient-primary rounded-xl shadow-xl"
                          initial={false}
                          transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
                        />
                      )}
                    </motion.div>
                  </Link>
                )
              })}
            </div>

            {/* Desktop Actions */}
            <div className="hidden md:flex items-center space-x-3">
              <Button variant="glass" size="sm">
                Войти
              </Button>
              <Button variant="primary" size="sm">
                Начать
              </Button>
            </div>

            {/* Mobile menu button */}
            <div className="md:hidden">
              <Button
                variant="ghost"
                size="sm"
                onClick={toggleMobileMenu}
                className="p-2"
              >
                {isMobileMenuOpen ? (
                  <X className="w-5 h-5" />
                ) : (
                  <Menu className="w-5 h-5" />
                )}
              </Button>
            </div>
          </div>
        </div>
      </nav>

      {/* Mobile Menu */}
      <motion.div
        className={cn(
          "md:hidden fixed inset-x-0 top-16 z-40 glass-dark border-b border-white/10",
          isMobileMenuOpen ? "block" : "hidden"
        )}
        initial={{ opacity: 0, y: -20 }}
        animate={{ 
          opacity: isMobileMenuOpen ? 1 : 0, 
          y: isMobileMenuOpen ? 0 : -20 
        }}
        transition={{ duration: 0.2 }}
      >
        <div className="px-4 py-4 space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon
            const isActive = location.pathname === item.path
            
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setIsMobileMenuOpen(false)}
              >
                <motion.div
                  className={cn(
                    'flex items-center space-x-3 px-4 py-3 rounded-xl transition-all',
                    isActive 
                      ? 'gradient-primary text-white shadow-xl' 
                      : 'text-white hover:bg-white/10'
                  )}
                  whileTap={{ scale: 0.98 }}
                >
                  <Icon className="w-5 h-5" />
                  <span className="font-medium">{item.label}</span>
                </motion.div>
              </Link>
            )
          })}
          <div className="pt-4 space-y-2">
            <Button variant="glass" size="sm" className="w-full">
              Войти
            </Button>
            <Button variant="primary" size="sm" className="w-full">
              Начать
            </Button>
          </div>
        </div>
      </motion.div>

      {/* Mobile Menu Overlay */}
      {isMobileMenuOpen && (
        <motion.div
          className="md:hidden fixed inset-0 bg-gray-900/50 backdrop-blur-sm z-30"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={() => setIsMobileMenuOpen(false)}
        />
      )}
    </>
  )
}

export default Navbar 