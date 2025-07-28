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
import { SplitText, BlurText, GlitchText, ShinyText } from '../../../shared/ui/animations'

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
      stats: 'Smart insights'
    }
  ]

  const metrics = [
    { value: '$3,540', label: 'Экономия в месяц', icon: Trophy },
    { value: '320%', label: 'ROI', icon: Star },
    { value: '10K+', label: 'Пользователей', icon: Users },
    { value: '99.2%', label: 'Точность AI', icon: Target }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-50 via-white to-primary-50">
      {/* Hero Section */}
      <section className="relative py-20 px-6 text-center overflow-hidden">
        <div className="absolute inset-0 bg-mesh opacity-30"></div>
        
        <MotionSlideIn direction="up" delay={0.1}>
          <div className="relative z-10 max-w-4xl mx-auto">
            <motion.div
              className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary-100 text-primary-700 text-sm font-medium mb-6"
              animate={{ scale: [1, 1.05, 1] }}
              transition={{ duration: 2, repeat: Infinity }}
            >
              <Sparkles className="w-4 h-4" />
              Революция в фитнесе началась!
            </motion.div>
            
            <SplitText 
              text="mvpFitness"
              className="text-5xl md:text-7xl font-black text-gradient-primary mb-6 block"
              splitType="chars"
              delay={300}
              stagger={0.08}
              duration={0.6}
            />
            
            <BlurText 
              text="Премиальная AI-платформа для фитнеса с анализом фото еды и персональным тренером нового поколения"
              className="text-xl md:text-2xl text-neutral-600 mb-8 max-w-3xl mx-auto block"
              direction="bottom"
              delay={800}
              duration={0.8}
            />
            
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button 
                variant="primary" 
                size="lg"
                leftIcon={<Play className="w-5 h-5" />}
              >
                <GlitchText 
                  text="Попробовать бесплатно" 
                  enableOnHover={true}
                  glitchIntensity="low"
                />
              </Button>
              <Button 
                variant="secondary" 
                size="lg"
                leftIcon={<Sparkles className="w-5 h-5" />}
              >
                Узнать больше
              </Button>
            </div>
          </div>
        </MotionSlideIn>
      </section>

      {/* Metrics Section */}
      <section className="py-16 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-16">
            {metrics.map((metric, index) => (
              <MotionSlideIn key={metric.label} direction="up" delay={0.2 + index * 0.1}>
                <Card variant="glass" className="text-center p-6">
                  <metric.icon className="w-8 h-8 mx-auto mb-3 text-primary-600" />
                  <div className="text-3xl font-bold text-gradient-primary mb-2">
                    {metric.value}
                  </div>
                  <div className="text-sm text-neutral-600">{metric.label}</div>
                </Card>
              </MotionSlideIn>
            ))}
          </div>
        </div>
      </section>

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
      <section className="py-16 px-6 bg-neutral-50">
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
    </div>
  )
}

export default HomePage 