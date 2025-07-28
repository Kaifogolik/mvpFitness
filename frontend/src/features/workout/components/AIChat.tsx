import React, { useState } from 'react'
import { motion } from 'framer-motion'
import { 
  Send, Zap, Brain, Star, Bot, User
} from 'lucide-react'
import { Button } from '../../../shared/ui/Button'
import { Card } from '../../../shared/ui/Card'
import { MotionSlideIn } from '../../../shared/ui/MotionSlideIn'

interface Message {
  id: string
  type: 'user' | 'ai'
  content: string
  timestamp: Date
}

const AIChat: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      type: 'ai',
      content: 'Привет! Я ваш персональный AI-тренер. Расскажите о ваших фитнес-целях, и я помогу составить план тренировок и питания.',
      timestamp: new Date()
    }
  ])
  const [inputMessage, setInputMessage] = useState('')
  const [isTyping, setIsTyping] = useState(false)

  const handleSendMessage = () => {
    if (!inputMessage.trim()) return

    const userMessage: Message = {
      id: Date.now().toString(),
      type: 'user',
      content: inputMessage,
      timestamp: new Date()
    }

    setMessages(prev => [...prev, userMessage])
    setInputMessage('')
    setIsTyping(true)

    // Simulate AI response
    setTimeout(() => {
      const aiMessage: Message = {
        id: (Date.now() + 1).toString(),
        type: 'ai',
        content: `Отличный вопрос! Основываясь на вашем сообщении "${inputMessage}", я рекомендую начать с базовых упражнений. Хотите персональный план тренировок?`,
        timestamp: new Date()
      }
      setMessages(prev => [...prev, aiMessage])
      setIsTyping(false)
    }, 2000)
  }

  const suggestions = [
    'Создай план тренировок на неделю',
    'Помоги с питанием для похудения',
    'Как набрать мышечную массу?',
    'Кардио или силовые тренировки?'
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-50 via-white to-primary-50 py-20 px-6">
      <div className="max-w-4xl mx-auto">
        
        {/* Header */}
        <MotionSlideIn direction="up" delay={0.1}>
          <div className="text-center mb-8">
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-secondary-100 text-secondary-700 rounded-full text-sm font-medium mb-6">
              <Brain className="w-4 h-4" />
              LLM Router • DeepSeek + Gemini
            </div>
            <h1 className="text-5xl font-bold text-gradient-primary mb-4">
              AI Персональный Тренер
            </h1>
            <p className="text-xl text-neutral-600">
              Умный помощник для достижения ваших фитнес-целей
            </p>
          </div>
        </MotionSlideIn>

        {/* Chat Container */}
        <MotionSlideIn direction="up" delay={0.2}>
          <Card variant="glass" className="p-6 mb-6">
            
            {/* Messages */}
            <div className="h-96 overflow-y-auto mb-6 space-y-4">
              {messages.map((message, index) => (
                <motion.div
                  key={message.id}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.1 }}
                  className={`flex ${message.type === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`flex items-start gap-3 max-w-xs lg:max-w-md ${
                    message.type === 'user' ? 'flex-row-reverse' : 'flex-row'
                  }`}>
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                      message.type === 'user' 
                        ? 'bg-primary-500 text-white'
                        : 'bg-secondary-500 text-white'
                    }`}>
                      {message.type === 'user' ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
                    </div>
                    <div className={`p-3 rounded-2xl ${
                      message.type === 'user'
                        ? 'bg-primary-500 text-white rounded-br-none'
                        : 'bg-neutral-100 text-neutral-800 rounded-bl-none'
                    }`}>
                      <p className="text-sm">{message.content}</p>
                    </div>
                  </div>
                </motion.div>
              ))}
              
              {/* Typing indicator */}
              {isTyping && (
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="flex justify-start"
                >
                  <div className="flex items-start gap-3">
                    <div className="w-8 h-8 rounded-full bg-secondary-500 text-white flex items-center justify-center">
                      <Bot className="w-4 h-4" />
                    </div>
                    <div className="bg-neutral-100 p-3 rounded-2xl rounded-bl-none">
                      <div className="flex space-x-1">
                        <div className="w-2 h-2 bg-neutral-400 rounded-full animate-bounce"></div>
                        <div className="w-2 h-2 bg-neutral-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                        <div className="w-2 h-2 bg-neutral-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}
            </div>

            {/* Input */}
            <div className="flex gap-3">
              <input
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                placeholder="Напишите ваш вопрос..."
                className="flex-1 px-4 py-3 border border-neutral-200 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 bg-white"
                disabled={isTyping}
              />
              <Button
                variant="primary"
                onClick={handleSendMessage}
                disabled={isTyping || !inputMessage.trim()}
                leftIcon={<Send className="w-4 h-4" />}
              >
                Отправить
              </Button>
            </div>

          </Card>
        </MotionSlideIn>

        {/* Quick Suggestions */}
        <MotionSlideIn direction="up" delay={0.3}>
          <div className="text-center">
            <h3 className="text-lg font-semibold text-neutral-700 mb-4">
              Популярные вопросы:
            </h3>
            <div className="flex flex-wrap justify-center gap-3">
              {suggestions.map((suggestion, index) => (
                <motion.button
                  key={index}
                  onClick={() => setInputMessage(suggestion)}
                  className="px-4 py-2 bg-white border border-neutral-200 rounded-lg text-sm hover:bg-neutral-50 transition-colors"
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                >
                  {suggestion}
                </motion.button>
              ))}
            </div>
          </div>
        </MotionSlideIn>

        {/* Features */}
        <MotionSlideIn direction="up" delay={0.4}>
          <div className="grid md:grid-cols-3 gap-6 mt-12">
            <Card variant="glass" className="p-6 text-center">
              <Zap className="w-12 h-12 mx-auto mb-4 text-primary-600" />
              <h3 className="text-lg font-bold mb-2">Мгновенные ответы</h3>
              <p className="text-neutral-600 text-sm">Получайте персональные рекомендации в реальном времени</p>
            </Card>
            <Card variant="glass" className="p-6 text-center">
              <Brain className="w-12 h-12 mx-auto mb-4 text-secondary-600" />
              <h3 className="text-lg font-bold mb-2">Умный анализ</h3>
              <p className="text-neutral-600 text-sm">AI анализирует ваши цели и создает оптимальный план</p>
            </Card>
            <Card variant="glass" className="p-6 text-center">
              <Star className="w-12 h-12 mx-auto mb-4 text-success-600" />
              <h3 className="text-lg font-bold mb-2">Персонализация</h3>
              <p className="text-neutral-600 text-sm">Индивидуальный подход к каждому пользователю</p>
            </Card>
          </div>
        </MotionSlideIn>

      </div>
    </div>
  )
}

export default AIChat 