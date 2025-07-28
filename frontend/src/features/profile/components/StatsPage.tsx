import React from 'react'
import { motion } from 'framer-motion'
import { 
  BarChart3, Target, TrendingUp, Flame, Award, Calendar,
  Activity, Star, ArrowRight, Sparkles
} from 'lucide-react'
import { Button } from '../../../shared/ui/Button'
import { Card } from '../../../shared/ui/Card'
import { NutrientBadge } from '../../../shared/ui/NutrientBadge'
import { MotionSlideIn } from '../../../shared/ui/MotionSlideIn'

const StatsPage: React.FC = () => {
  const stats = [
    {
      label: 'Дни активности',
      value: '24',
      trend: '+8.2%',
      icon: Activity,
      color: 'text-success-600'
    },
    {
      label: 'Сожжено калорий',
      value: '12,540',
      trend: '+15.3%',
      icon: Flame,
      color: 'text-secondary-600'
    },
    {
      label: 'Достижения',
      value: '18',
      trend: '+6 новых',
      icon: Award,
      color: 'text-primary-600'
    },
    {
      label: 'Цели выполнено',
      value: '85%',
      trend: '+12%',
      icon: Target,
      color: 'text-success-600'
    }
  ]

  const recentMeals = [
    { name: 'Овсянка с ягодами', calories: 320, protein: 12, time: 'Завтрак' },
    { name: 'Куриная грудка с рисом', calories: 420, protein: 35, time: 'Обед' },
    { name: 'Греческий йогурт', calories: 150, protein: 15, time: 'Перекус' }
  ]

  const achievements = [
    { title: '7 дней подряд', description: 'Ежедневные тренировки', icon: '🔥' },
    { title: 'Цель по калориям', description: 'Достигнута на 100%', icon: '🎯' },
    { title: 'Новый рекорд', description: '10K шагов за день', icon: '👟' },
    { title: 'Водный баланс', description: '2.5L воды в день', icon: '💧' }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-50 via-white to-primary-50 py-20 px-6">
      <div className="max-w-7xl mx-auto">
        
        {/* Header */}
        <MotionSlideIn direction="up" delay={0.1}>
          <div className="text-center mb-12">
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-success-100 text-success-700 rounded-full text-sm font-medium mb-6">
              <BarChart3 className="w-4 h-4" />
              Real-time Analytics
            </div>
            <h1 className="text-5xl font-bold text-gradient-primary mb-4">
              Ваш Прогресс
            </h1>
            <p className="text-xl text-neutral-600">
              Детальная аналитика достижений и трендов
            </p>
          </div>
        </MotionSlideIn>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {stats.map((stat, index) => (
            <MotionSlideIn key={stat.label} direction="up" delay={0.2 + index * 0.1}>
              <Card variant="glass" className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <stat.icon className={`w-8 h-8 ${stat.color}`} />
                  <span className="text-sm font-medium text-success-600">{stat.trend}</span>
                </div>
                <div className="text-3xl font-bold text-gradient-primary mb-1">
                  {stat.value}
                </div>
                <div className="text-sm text-neutral-600">{stat.label}</div>
              </Card>
            </MotionSlideIn>
          ))}
        </div>

        <div className="grid lg:grid-cols-3 gap-8">
          
          {/* Progress Chart */}
          <MotionSlideIn direction="left" delay={0.3} className="lg:col-span-2">
            <Card variant="glass" className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold">Прогресс за месяц</h2>
                <Button variant="ghost" size="sm">
                  <Calendar className="w-4 h-4 mr-2" />
                  Настроить период
                </Button>
              </div>
              
              {/* Mock Chart */}
              <div className="h-64 bg-neutral-50 rounded-xl flex items-end justify-center p-4">
                <div className="flex items-end gap-2 h-full">
                  {[40, 65, 45, 80, 55, 90, 70].map((height, index) => (
                    <motion.div
                      key={index}
                      className="bg-gradient-to-t from-primary-500 to-secondary-500 rounded-t-lg w-8"
                      style={{ height: `${height}%` }}
                      initial={{ height: 0 }}
                      animate={{ height: `${height}%` }}
                      transition={{ delay: 0.5 + index * 0.1, duration: 0.5 }}
                    />
                  ))}
                </div>
              </div>
              
              <div className="mt-4 flex justify-center gap-6 text-sm text-neutral-600">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-primary-500 rounded"></div>
                  Тренировки
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-secondary-500 rounded"></div>
                  Питание
                </div>
              </div>
            </Card>
          </MotionSlideIn>

          {/* Quick Actions */}
          <MotionSlideIn direction="right" delay={0.4}>
            <Card variant="glass" className="p-6">
              <h3 className="text-xl font-bold mb-6">Быстрые действия</h3>
              <div className="space-y-3">
                <Button variant="ghost" className="w-full justify-start">
                  <Activity className="w-4 h-4 mr-3" />
                  Новая тренировка
                  <ArrowRight className="w-4 h-4 ml-auto" />
                </Button>
                <Button variant="ghost" className="w-full justify-start">
                  <Target className="w-4 h-4 mr-3" />
                  Установить цель
                  <ArrowRight className="w-4 h-4 ml-auto" />
                </Button>
                <Button variant="ghost" className="w-full justify-start">
                  <BarChart3 className="w-4 h-4 mr-3" />
                  Экспорт данных
                  <ArrowRight className="w-4 h-4 ml-auto" />
                </Button>
              </div>
            </Card>
          </MotionSlideIn>
        </div>

        <div className="grid md:grid-cols-2 gap-8 mt-8">
          
          {/* Recent Meals */}
          <MotionSlideIn direction="up" delay={0.5}>
            <Card variant="glass" className="p-6">
              <h3 className="text-xl font-bold mb-6">Последние приемы пищи</h3>
              <div className="space-y-4">
                {recentMeals.map((meal, index) => (
                  <motion.div
                    key={index}
                    className="flex items-center justify-between p-3 bg-white rounded-lg"
                    whileHover={{ scale: 1.02 }}
                  >
                    <div className="flex-1">
                      <div className="font-medium">{meal.name}</div>
                      <div className="text-sm text-neutral-600">{meal.time}</div>
                    </div>
                    <div className="flex gap-2">
                      <NutrientBadge type="calories" value={meal.calories} size="sm" />
                      <NutrientBadge type="protein" value={meal.protein} size="sm" />
                    </div>
                  </motion.div>
                ))}
              </div>
            </Card>
          </MotionSlideIn>

          {/* Achievements */}
          <MotionSlideIn direction="up" delay={0.6}>
            <Card variant="glass" className="p-6">
              <h3 className="text-xl font-bold mb-6">Недавние достижения</h3>
              <div className="space-y-4">
                {achievements.map((achievement, index) => (
                  <motion.div
                    key={index}
                    className="flex items-center gap-4 p-3 bg-white rounded-lg"
                    whileHover={{ scale: 1.02 }}
                  >
                    <div className="text-2xl">{achievement.icon}</div>
                    <div className="flex-1">
                      <div className="font-medium">{achievement.title}</div>
                      <div className="text-sm text-neutral-600">{achievement.description}</div>
                    </div>
                    <Star className="w-5 h-5 text-yellow-500" />
                  </motion.div>
                ))}
              </div>
            </Card>
          </MotionSlideIn>
        </div>

        {/* CTA */}
        <MotionSlideIn direction="up" delay={0.7}>
          <div className="text-center mt-12">
            <Card variant="primary" className="p-8 text-center text-white">
              <TrendingUp className="w-16 h-16 mx-auto mb-4" />
              <h2 className="text-3xl font-bold mb-4">Продолжайте в том же духе!</h2>
              <p className="text-xl opacity-90 mb-6">
                Вы на правильном пути к достижению своих целей
              </p>
              <Button variant="glass" size="lg">
                <Sparkles className="w-5 h-5 mr-2" />
                Поделиться успехом
              </Button>
            </Card>
          </div>
        </MotionSlideIn>

      </div>
    </div>
  )
}

export default StatsPage 