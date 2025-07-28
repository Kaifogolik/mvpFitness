import React, { useState } from 'react'
import { motion } from 'framer-motion'
import { 
  Search, Database, TrendingUp,
  Star, ArrowRight
} from 'lucide-react'
import { Button } from '../../../shared/ui/Button'
import { Card } from '../../../shared/ui/Card'
import { NutrientBadge } from '../../../shared/ui/NutrientBadge'

const NutritionPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('')

  const popularProducts = [
    {
      name: 'Куриная грудка',
      category: 'Мясо',
      calories: 165,
      protein: 31,
      carbs: 0,
      fat: 3.6,
      rating: 4.9,
      searches: '15K за месяц'
    },
    {
      name: 'Овсяная каша',
      category: 'Крупы',
      calories: 68,
      protein: 2.4,
      carbs: 12,
      fat: 1.4,
      rating: 4.7,
      searches: '12K за месяц'
    },
    {
      name: 'Авокадо',
      category: 'Фрукты',
      calories: 160,
      protein: 2,
      carbs: 9,
      fat: 15,
      rating: 4.8,
      searches: '8.5K за месяц'
    }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-50 via-white to-primary-50 py-20 px-6">
      <div className="max-w-6xl mx-auto">
        <motion.div 
          className="text-center mb-12"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <div className="inline-flex items-center gap-2 px-4 py-2 bg-success-100 text-success-700 rounded-full text-sm font-medium mb-6">
            <Database className="w-4 h-4" />
            FatSecret API • 500K+ продуктов
          </div>
          <h1 className="text-5xl font-bold text-gradient-primary mb-4">
            База Продуктов
          </h1>
          <p className="text-xl text-neutral-600 mb-8">
            Крупнейшая база данных продуктов с точными пищевыми характеристиками
          </p>
        </motion.div>

        <div className="max-w-2xl mx-auto mb-12">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
            <input
              type="text"
              placeholder="Поиск продуктов..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-12 pr-4 py-4 text-lg border border-neutral-200 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 bg-white/80 backdrop-blur-sm"
            />
          </div>
        </div>

        <div className="mb-12">
          <div className="flex items-center gap-2 mb-6">
            <TrendingUp className="w-6 h-6 text-primary-600" />
            <h2 className="text-2xl font-bold">Популярные продукты</h2>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {popularProducts.map((product, index) => (
              <motion.div
                key={product.name}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
              >
                <Card variant="glass" className="p-6 hover-lift">
                  <div className="flex items-start justify-between mb-4">
                    <div>
                      <h3 className="text-lg font-bold mb-1">{product.name}</h3>
                      <p className="text-sm text-neutral-600">{product.category}</p>
                    </div>
                    <div className="flex items-center gap-1">
                      <Star className="w-4 h-4 text-yellow-400 fill-current" />
                      <span className="text-sm font-medium">{product.rating}</span>
                    </div>
                  </div>
                  
                  <div className="flex flex-wrap gap-2 mb-4">
                    <NutrientBadge type="calories" value={product.calories} size="sm" />
                    <NutrientBadge type="protein" value={product.protein} size="sm" />
                    <NutrientBadge type="carbs" value={product.carbs} size="sm" />
                    <NutrientBadge type="fat" value={product.fat} size="sm" />
                  </div>
                  
                  <div className="flex items-center justify-between text-sm text-neutral-500">
                    <span>{product.searches}</span>
                    <ArrowRight className="w-4 h-4" />
                  </div>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>

        <div className="text-center">
          <Button variant="primary" size="lg">
            <Database className="w-5 h-5 mr-2" />
            Загрузить больше продуктов
          </Button>
        </div>
      </div>
    </div>
  )
}

export default NutritionPage 