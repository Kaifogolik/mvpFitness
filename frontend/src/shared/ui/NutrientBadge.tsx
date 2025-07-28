import React from 'react'
import { motion } from 'framer-motion'
import { cn } from '../utils/cn'

type NutrientType = 'calories' | 'protein' | 'carbs' | 'fat' | 'fiber' | 'sugar' | 'custom'

interface NutrientBadgeProps {
  type: NutrientType
  value: number | string
  unit?: string
  label?: string
  variant?: 'default' | 'outlined' | 'glass' | 'gradient'
  size?: 'sm' | 'md' | 'lg'
  showIcon?: boolean
  animate?: boolean
  className?: string
  customColor?: string
}

const NutrientBadge: React.FC<NutrientBadgeProps> = ({
  type,
  value,
  unit = 'г',
  label,
  variant = 'default',
  size = 'md',
  showIcon = true,
  animate = true,
  className,
  customColor
}) => {
  const nutrientConfig = {
    calories: {
      label: 'Калории',
      unit: 'ккал',
      icon: '🔥',
      color: 'text-secondary-600 bg-secondary-50 border-secondary-200',
      gradientColor: 'from-secondary-500 to-secondary-600'
    },
    protein: {
      label: 'Белки',
      unit: 'г',
      icon: '💪',
      color: 'text-primary-600 bg-primary-50 border-primary-200',
      gradientColor: 'from-primary-500 to-primary-600'
    },
    carbs: {
      label: 'Углеводы',
      unit: 'г',
      icon: '🌾',
      color: 'text-secondary-600 bg-secondary-50 border-secondary-200',
      gradientColor: 'from-secondary-400 to-secondary-500'
    },
    fat: {
      label: 'Жиры',
      unit: 'г',
      icon: '🥑',
      color: 'text-success-600 bg-success-50 border-success-200',
      gradientColor: 'from-success-500 to-success-600'
    },
    fiber: {
      label: 'Клетчатка',
      unit: 'г',
      icon: '🌿',
      color: 'text-emerald-600 bg-emerald-50 border-emerald-200',
      gradientColor: 'from-emerald-500 to-green-500'
    },
    sugar: {
      label: 'Сахар',
      unit: 'г',
      icon: '🍯',
      color: 'text-amber-600 bg-amber-50 border-amber-200',
      gradientColor: 'from-amber-500 to-yellow-500'
    },
    custom: {
      label: label || 'Пищевая ценность',
      unit: unit,
      icon: '📊',
      color: customColor || 'text-neutral-600 bg-neutral-50 border-neutral-200',
      gradientColor: 'from-neutral-500 to-neutral-600'
    }
  }

  const config = nutrientConfig[type]
  const finalLabel = label || config.label
  const finalUnit = unit || config.unit

  const sizes = {
    sm: 'text-xs px-2 py-1 gap-1',
    md: 'text-sm px-3 py-1.5 gap-1.5',
    lg: 'text-base px-4 py-2 gap-2'
  }

  const variants = {
    default: cn('border', config.color),
    outlined: cn('border-2 bg-transparent', config.color),
    glass: 'bg-white/20 backdrop-blur-sm border border-white/30 text-white',
    gradient: cn('bg-gradient-to-r text-white border-0', config.gradientColor)
  }

  const containerClassName = cn(
    'inline-flex items-center justify-center rounded-full font-medium transition-all duration-200',
    'hover:scale-105 active:scale-95',
    sizes[size],
    variants[variant],
    className
  )

  const badgeContent = (
    <>
      {showIcon && (
                 <motion.span
           className="text-sm"
           initial={animate ? { scale: 0 } : {}}
           animate={animate ? { scale: 1 } : {}}
           transition={{ delay: 0.1, type: 'spring', stiffness: 500 }}
         >
           {config.icon}
         </motion.span>
      )}
      
             <motion.span
         className="font-semibold"
         initial={animate ? { opacity: 0, x: -10 } : {}}
         animate={animate ? { opacity: 1, x: 0 } : {}}
         transition={{ delay: 0.2, duration: 0.3 }}
       >
        {value}
        <span className="text-xs opacity-80 ml-0.5">{finalUnit}</span>
      </motion.span>
      
      {size === 'lg' && (
                 <motion.span
           className="text-xs opacity-70 hidden sm:inline"
           initial={animate ? { opacity: 0 } : {}}
           animate={animate ? { opacity: 0.7 } : {}}
           transition={{ delay: 0.3, duration: 0.3 }}
         >
          {finalLabel}
        </motion.span>
      )}
    </>
  )

  if (animate) {
    return (
      <motion.div
        className={containerClassName}
        initial={{ opacity: 0, scale: 0.8, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{
          type: 'spring',
          stiffness: 300,
          damping: 20,
          opacity: { duration: 0.3 }
        }}
                 whileHover={{ 
           scale: 1.05,
           ...(variant === 'gradient' && { boxShadow: '0 4px 20px rgba(0,0,0,0.15)' })
         }}
        whileTap={{ scale: 0.95 }}
      >
        {badgeContent}
      </motion.div>
    )
  }

  return (
    <div className={containerClassName}>
      {badgeContent}
    </div>
  )
}

export { NutrientBadge, type NutrientBadgeProps, type NutrientType } 