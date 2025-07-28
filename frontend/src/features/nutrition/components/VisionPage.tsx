import React from 'react'
import { motion } from 'framer-motion'
import { Camera, Upload, Zap, Target, TrendingUp,
  CheckCircle, Eye } from 'lucide-react'
import { Button } from '../../../shared/ui/Button'
import { Card } from '../../../shared/ui/Card'
import { GradientCard } from '../../../shared/ui/GradientCard'
import { NutrientBadge } from '../../../shared/ui/NutrientBadge'

const VisionPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-50 via-white to-primary-50 py-20 px-6">
      <div className="max-w-4xl mx-auto">
        <motion.div 
          className="text-center mb-12"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <div className="inline-flex items-center gap-2 px-4 py-2 bg-primary-100 text-primary-700 rounded-full text-sm font-medium mb-6">
            <Eye className="w-4 h-4" />
            Computer Vision AI
          </div>
          <h1 className="text-5xl font-bold text-gradient-primary mb-4">
            AI Анализ Фото
          </h1>
          <p className="text-xl text-neutral-600 mb-8">
            Сфотографируйте любое блюдо и получите мгновенный анализ КБЖУ
          </p>
        </motion.div>

        <GradientCard gradient="primary" className="p-12 text-center mb-8">
          <Camera className="w-24 h-24 mx-auto mb-6 text-white" />
          <h2 className="text-3xl font-bold text-white mb-4">
            Загрузите фото еды
          </h2>
          <p className="text-white/90 mb-8 text-lg">
            Наш AI определит продукты и рассчитает точные пищевые характеристики
          </p>
          <div className="flex gap-4 justify-center">
            <Button variant="glass" size="lg">
              <Camera className="w-5 h-5 mr-2" />
              Сделать фото
            </Button>
            <Button variant="secondary" size="lg">
              <Upload className="w-5 h-5 mr-2" />
              Загрузить файл
            </Button>
          </div>
        </GradientCard>

        <div className="grid md:grid-cols-3 gap-6">
          <Card variant="glass" className="p-6 text-center">
            <Target className="w-12 h-12 mx-auto mb-4 text-primary-600" />
            <h3 className="text-xl font-bold mb-2">99.2% точность</h3>
            <p className="text-neutral-600">Профессиональное распознавание</p>
          </Card>
          <Card variant="glass" className="p-6 text-center">
            <Zap className="w-12 h-12 mx-auto mb-4 text-secondary-600" />
            <h3 className="text-xl font-bold mb-2">Мгновенно</h3>
            <p className="text-neutral-600">Результат за 2-3 секунды</p>
          </Card>
          <Card variant="glass" className="p-6 text-center">
            <TrendingUp className="w-12 h-12 mx-auto mb-4 text-success-600" />
            <h3 className="text-xl font-bold mb-2">Smart анализ</h3>
            <p className="text-neutral-600">ИИ рекомендации по питанию</p>
          </Card>
        </div>

        <div className="mt-12 p-8 bg-neutral-50 rounded-2xl">
          <h3 className="text-2xl font-bold text-center mb-6">Пример результата</h3>
          <div className="bg-white p-6 rounded-xl shadow-lg">
            <div className="flex items-center gap-4 mb-4">
              <CheckCircle className="w-6 h-6 text-success-500" />
              <h4 className="text-xl font-bold">Куриная грудка с овощами</h4>
            </div>
            <div className="flex flex-wrap gap-3 mb-4">
              <NutrientBadge type="calories" value={245} variant="gradient" />
              <NutrientBadge type="protein" value={42} variant="default" />
              <NutrientBadge type="carbs" value={8} variant="outlined" />
              <NutrientBadge type="fat" value={5} variant="glass" />
            </div>
            <p className="text-neutral-600">
              💡 <strong>Рекомендация AI:</strong> Отличный выбор для набора мышечной массы! 
              Добавьте немного углеводов для лучшего восстановления.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default VisionPage 