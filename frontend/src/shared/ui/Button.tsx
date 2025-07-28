import React from 'react'
import { motion } from 'framer-motion'
import { Loader2 } from 'lucide-react'
import { cn } from '../utils/cn'

interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'ghost' | 'glass'
  size?: 'sm' | 'md' | 'lg' | 'xl'
  isLoading?: boolean
  leftIcon?: React.ReactNode
  rightIcon?: React.ReactNode
  children: React.ReactNode
  className?: string
  disabled?: boolean
  onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void
  type?: 'button' | 'submit' | 'reset'
  fullWidth?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({
    className,
    variant = 'primary',
    size = 'md',
    isLoading = false,
    leftIcon,
    rightIcon,
    children,
    disabled,
    onClick,
    type = 'button',
    fullWidth = false,
    ...props
  }, ref) => {
    const variants = {
      primary: 'btn-primary',
      secondary: 'btn-secondary',
      success: 'btn-success',
      danger: 'btn-danger',
      ghost: 'btn-ghost',
      glass: 'btn-glass'
    }

    const sizes = {
      sm: 'btn-sm',
      md: '', // default size in btn class
      lg: 'btn-lg',
      xl: 'btn-xl'
    }

    return (
      <motion.button
        ref={ref}
        type={type}
        className={cn(
          'btn',
          variants[variant],
          sizes[size],
          fullWidth && 'w-full',
          isLoading && 'cursor-not-allowed opacity-70',
          disabled && 'cursor-not-allowed opacity-50',
          className
        )}
        disabled={disabled || isLoading}
        onClick={onClick}
        whileHover={{ 
          scale: disabled || isLoading ? 1 : 1.02,
          boxShadow: variant === 'primary' ? '0 0 25px rgba(168, 85, 247, 0.5)' : undefined
        }}
        whileTap={{ scale: disabled || isLoading ? 1 : 0.98 }}
        transition={{ type: "spring", stiffness: 400, damping: 17 }}
        {...props}
      >
        {isLoading && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="mr-2"
          >
            <Loader2 className="w-4 h-4 animate-spin" />
          </motion.div>
        )}
        {!isLoading && leftIcon && (
          <motion.span 
            className="mr-2"
            initial={{ opacity: 0, x: -5 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1 }}
          >
            {leftIcon}
          </motion.span>
        )}
        <motion.span
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.1 }}
        >
          {children}
        </motion.span>
        {!isLoading && rightIcon && (
          <motion.span 
            className="ml-2"
            initial={{ opacity: 0, x: 5 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1 }}
          >
            {rightIcon}
          </motion.span>
        )}
      </motion.button>
    )
  }
)

Button.displayName = 'Button'

export { Button, type ButtonProps } 