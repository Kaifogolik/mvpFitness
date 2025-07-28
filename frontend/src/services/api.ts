import axios from 'axios'

// Base API configuration
const API_BASE_URL = 'http://localhost:8080'

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add auth token if needed
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle common errors
    if (error.response?.status === 401) {
      // Unauthorized - clear auth
      localStorage.removeItem('auth_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// API Types
export interface NutritionSearchResult {
  success: boolean
  nutrition?: {
    name: string
    weight: number
    calories: number
    macros: {
      protein: number
      carbohydrates: number
      fat: number
    }
    additional: {
      fiber: number
      sugar: number
      sodium: number
    }
    vitamins: {
      vitaminC?: number
      calcium?: number
      iron?: number
      potassium?: number
    }
    metadata: {
      source: string
      fetchedAt: string
      valid: boolean
    }
  }
  source?: string
  message?: string
  error?: string
  suggestion?: string
}

export interface VisionAnalysisResult {
  success: boolean
  detection?: {
    foodName: string
    confidence: number
    confidencePercent: number
    estimatedWeight: number
    category: string
    summary: string
    alternatives?: Array<{
      name: string
      confidence: number
      category: string
    }>
    boundingBox?: {
      x: number
      y: number
      width: number
      height: number
      area: number
    }
    imageMetadata?: {
      width: number
      height: number
      format: string
      fileSizeBytes: number
      colorSpace: string
      brightness?: number
      contrast?: number
      goodQuality: boolean
    }
    analysisMetadata: {
      modelVersion: string
      analyzedAt: string
      processingType: string
      costUsd: number
    }
  }
  confidence?: string
  message?: string
  error?: string
  suggestion?: string
}

export interface AIResponse {
  success: boolean
  content?: string
  provider?: string
  model?: string
  tokensUsed?: number
  costUsd?: number
  processingTimeMs?: number
  error?: string
}

export interface APIStatistics {
  success: boolean
  statistics?: {
    sources: {
      fatSecret: {
        available: boolean
        details: string
      }
      usda: {
        available: boolean
        details: string
      }
      edamam: {
        available: boolean
        details: string
      }
      localDb: {
        available: boolean
        details: string
      }
    }
    performance: {
      totalRequests: number
      cacheHitRate: string
      averageResponseTime: string
    }
    economics: {
      monthlySavings: string
      yearlyProjection: string
      costPerRequest: string
      vsCommercialApis: string
    }
  }
  anySourceAvailable?: boolean
}

// Nutrition API
export const nutritionApi = {
  search: async (foodName: string, weight: number = 100): Promise<NutritionSearchResult> => {
    const response = await api.get(`/api/v2/nutrition/search`, {
      params: { foodName, weight }
    })
    return response.data
  },

  batchSearch: async (foods: Array<{ name: string; weight?: number }>): Promise<{ results: any[] }> => {
    const response = await api.post(`/api/v2/nutrition/batch-search`, { foods })
    return response.data
  },

  getStatistics: async (): Promise<APIStatistics> => {
    const response = await api.get(`/api/v2/nutrition/statistics`)
    return response.data
  },

  clearCache: async (): Promise<{ success: boolean; message: string }> => {
    const response = await api.delete(`/api/v2/nutrition/cache`)
    return response.data
  },

  healthCheck: async (): Promise<{ success: boolean; healthy: boolean; status: string }> => {
    const response = await api.get(`/api/v2/nutrition/health`)
    return response.data
  }
}

// Vision API
export const visionApi = {
  analyzeImage: async (imageFile: File, userId?: string): Promise<VisionAnalysisResult> => {
    const formData = new FormData()
    formData.append('image', imageFile)
    if (userId) {
      formData.append('userId', userId)
    }

    const response = await api.post(`/api/v2/vision/analyze-image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  validateImage: async (imageFile: File): Promise<{ valid: boolean; message: string }> => {
    const formData = new FormData()
    formData.append('image', imageFile)

    const response = await api.post(`/api/v2/vision/validate-image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  getModelInfo: async (): Promise<{ model: string; version: string; accuracy: number }> => {
    const response = await api.get(`/api/v2/vision/model-info`)
    return response.data
  },

  healthCheck: async (): Promise<{ success: boolean; healthy: boolean; status: string }> => {
    const response = await api.get(`/api/v2/vision/health`)
    return response.data
  }
}

// AI Router API
export const aiApi = {
  analyzeFood: async (content: string, userId: string): Promise<AIResponse> => {
    const response = await api.post(`/api/v2/ai/analyze-food`, 
      { content }, 
      { params: { userId } }
    )
    return response.data
  },

  getNutritionAdvice: async (content: string, userId: string): Promise<AIResponse> => {
    const response = await api.post(`/api/v2/ai/nutrition-advice`, 
      { content }, 
      { params: { userId } }
    )
    return response.data
  },

  createWorkoutPlan: async (content: string, userId: string): Promise<AIResponse> => {
    const response = await api.post(`/api/v2/ai/workout-plan`, 
      { content }, 
      { params: { userId } }
    )
    return response.data
  },

  analyzeProgress: async (content: string, userId: string): Promise<AIResponse> => {
    const response = await api.post(`/api/v2/ai/analyze-progress`, 
      { content }, 
      { params: { userId } }
    )
    return response.data
  },

  complexQuery: async (content: string, userId: string): Promise<AIResponse> => {
    const response = await api.post(`/api/v2/ai/complex-query`, 
      { content }, 
      { params: { userId } }
    )
    return response.data
  },

  getStatistics: async (): Promise<{ requests: number; accuracy: number; uptime: string }> => {
    const response = await api.get(`/api/v2/ai/statistics`)
    return response.data
  },

  clearCache: async (): Promise<{ success: boolean; message: string }> => {
    const response = await api.delete(`/api/v2/ai/cache`)
    return response.data
  }
}

// Health check for all services
export const healthApi = {
  checkAll: async () => {
    const results = await Promise.allSettled([
      nutritionApi.healthCheck(),
      visionApi.healthCheck(),
    ])

    return {
      nutrition: results[0].status === 'fulfilled' ? results[0].value : { success: false, healthy: false },
      vision: results[1].status === 'fulfilled' ? results[1].value : { success: false, healthy: false },
    }
  }
}

export default api 