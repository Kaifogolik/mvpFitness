import React from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { 
  Camera, Apple, MessageCircle, BarChart3, Target,
  Trophy, Star, Flame, Users, ArrowRight, Sparkles, Play
} from 'lucide-react'
import { Button } from '../../../shared/ui/Button'
import { Card } from '../../../shared/ui/Card'
import { GradientCard } from '../../../shared/ui/GradientCard'
import { NutrientBadge } from '../../../shared/ui/NutrientBadge'
import { MotionSlideIn } from '../../../shared/ui/MotionSlideIn'
import Background from '../../../shared/ui/Background'
import { SplitText } from '../../../shared/ui/animations'
import { BlurText } from '../../../shared/ui/animations'
import { GlitchText } from '../../../shared/ui/animations'
import { ShinyText } from '../../../shared/ui/animations'

const HomePage: React.FC = () => {
  const features = [
    {
      icon: Camera,
      title: 'AI Анализ Фото',
      description: 'Сфотографируйте любое блюдо и получите мгновенный анализ КБЖУ с помощью нейросетей',
      path: '/vision',
      gradient: 'primary' as const,
      badge: 'Computer Vision',
      stats: '99.2% точность'
    },
    {
      icon: Apple,
      title: 'База Продуктов',
      description: 'Огромная база данных продуктов с точными пищевыми характеристиками',
      path: '/nutrition',
      gradient: 'success' as const,
      badge: 'FatSecret API',
      stats: '500K+ продуктов'
    },
    {
      icon: MessageCircle,
      title: 'AI Персональный Тренер',
      description: 'Умный помощник, который создает индивидуальные планы тренировок и питания',
      path: '/chat',
      gradient: 'secondary' as const,
      badge: 'LLM Router',
      stats: 'Мгновенные ответы'
    },
    {
      icon: BarChart3,
      title: 'Аналитика Прогресса',
      description: 'Детальная статистика и визуализация вашего прогресса',
      path: '/stats',
      gradient: 'primary' as const,
      badge: 'Real-time данные',
      stats: 'Полная аналитика'
    }
  ]

  const metrics = [
    { value: '500K+', label: 'Продуктов в базе', icon: Apple },
    { value: '99.2%', label: 'Точность ИИ', icon: Target },
    { value: '24/7', label: 'Поддержка', icon: MessageCircle },
    { value: '5 звезд', label: 'Рейтинг', icon: Star }
  ]

  return (
    <Background variant="fitness" animate={true} className="min-h-screen">
      {/* Hero Section с революционными анимациями */}
      <div className="relative pt-24 pb-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          {/* Главный заголовок с 3D эффектом */}
          <motion.div
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="mb-6"
          >
            <SplitText
              text="mvpFitness"
              splitType="chars"
              enableGradient={true}
              use3D={true}
              bounceEffect={true}
              className="text-6xl md:text-8xl font-black tracking-tight text-gradient-primary"
              delay={300}
              stagger={0.1}
            />
          </motion.div>

          {/* Подзаголовок с blur эффектом */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 1.2, duration: 0.8 }}
            className="mb-8"
          >
            <BlurText
              text="Революционная AI-платформа для фитнеса и питания"
              direction="bottom"
              className="text-xl md:text-2xl text-neutral-600 dark:text-neutral-300 max-w-3xl mx-auto"
              delay={1500}
              duration={1}
            />
          </motion.div>

          {/* Описание с дополнительным эффектом */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 2, duration: 0.8 }}
            className="mb-12"
          >
            <BlurText
              text="Используйте мощь искусственного интеллекта для анализа питания, персональных тренировок и достижения ваших фитнес-целей"
              direction="top"
              className="text-lg text-neutral-500 dark:text-neutral-400 max-w-2xl mx-auto leading-relaxed"
              delay={2200}
              duration={0.8}
            />
          </motion.div>

          {/* CTA Кнопки с глитч эффектом */}
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 2.8, duration: 0.6 }}
            className="flex flex-col sm:flex-row gap-4 justify-center mb-16"
          >
            <Link to="/vision">
              <Button size="lg" className="group relative overflow-hidden bg-gradient-primary text-white hover-glow">
                <GlitchText 
                  text="Попробовать бесплатно"
                  enableOnHover={true}
                  intensity="medium"
                  className="relative z-10"
                />
                <ArrowRight className="ml-2 h-5 w-5 transform group-hover:translate-x-1 transition-transform" />
              </Button>
            </Link>
            
            <Button variant="ghost" size="lg" className="group">
              <Play className="mr-2 h-5 w-5" />
              Смотреть демо
            </Button>
          </motion.div>

          {/* Metrics Cards */}
          <MotionSlideIn direction="up" delay={3.2}>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-16">
              {metrics.map((metric, index) => (
                <motion.div
                  key={metric.label}
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ 
                    delay: 3.5 + index * 0.1, 
                    duration: 0.5,
                    type: "spring",
                    stiffness: 200
                  }}
                >
                  <Card className="p-6 text-center glass-card hover-glow transform-gpu">
                    <metric.icon className="h-8 w-8 mx-auto mb-3 text-primary-500" />
                    <ShinyText
                      text={metric.value}
                      playOnHover={true}
                      className="text-2xl font-bold text-gradient-primary"
                    />
                    <p className="text-sm text-neutral-600 dark:text-neutral-400 mt-1">
                      {metric.label}
                    </p>
                  </Card>
                </motion.div>
              ))}
            </div>
          </MotionSlideIn>
        </div>
      </div>

      {/* Features Section */}
      <section className="py-16 px-6">
        <div className="max-w-6xl mx-auto">
          <MotionSlideIn direction="up" delay={0.3}>
            <div className="text-center mb-12">
              <h2 className="text-4xl font-bold text-gradient-primary mb-4">
                Передовые AI Технологии
              </h2>
              <p className="text-xl text-neutral-600 max-w-3xl mx-auto">
                Революционные функции, которые изменят ваш подход к фитнесу и питанию
              </p>
            </div>
          </MotionSlideIn>

          <div className="grid md:grid-cols-2 gap-8">
            {features.map((feature, index) => (
              <MotionSlideIn key={feature.title} direction="up" delay={0.4 + index * 0.1}>
                <Link to={feature.path}>
                  <GradientCard 
                    gradient={feature.gradient}
                    hover={true}
                    glow={true}
                    className="p-8 h-full"
                  >
                    <div className="flex items-start gap-4">
                      <div className="flex-shrink-0">
                        <feature.icon className="w-12 h-12 text-white" />
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-3">
                          <h3 className="text-2xl font-bold text-white">
                            <ShinyText 
                              text={feature.title}
                              speed={4}
                              playOnHover={true}
                              autoPlay={false}
                            />
                          </h3>
                          <span className="px-2 py-1 bg-white/20 rounded-full text-xs font-medium">
                            {feature.badge}
                          </span>
                        </div>
                        <p className="text-white/90 mb-4 leading-relaxed">
                          {feature.description}
                        </p>
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium text-white/80">
                            {feature.stats}
                          </span>
                          <ArrowRight className="w-5 h-5 text-white/60" />
                        </div>
                      </div>
                    </div>
                  </GradientCard>
                </Link>
              </MotionSlideIn>
            ))}
          </div>
        </div>
      </section>

      {/* Demo Section */}
      <section className="py-16 px-6">
        <div className="max-w-4xl mx-auto text-center">
          <MotionSlideIn direction="up" delay={0.5}>
            <h2 className="text-3xl font-bold text-gradient-primary mb-6">
              Попробуйте новые компоненты
            </h2>
            
            <div className="flex flex-wrap justify-center gap-4 mb-8">
              <NutrientBadge type="calories" value={165} variant="gradient" />
              <NutrientBadge type="protein" value={31} variant="default" />
              <NutrientBadge type="carbs" value={0} variant="outlined" />
              <NutrientBadge type="fat" value={3.6} variant="glass" />
            </div>
            
            <div className="bg-gradient-primary text-white p-8 rounded-2xl">
              <Flame className="w-12 h-12 mx-auto mb-4" />
              <h3 className="text-2xl font-bold mb-2">Enterprise-Ready!</h3>
              <p className="text-lg opacity-90">
                React 19 • TypeScript 5.5 • Framer Motion 12 • TailwindCSS 3.4
              </p>
            </div>
          </MotionSlideIn>
        </div>
      </section>
    </Background>
  )
}

export default HomePage 