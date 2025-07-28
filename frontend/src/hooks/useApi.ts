import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { nutritionApi, visionApi, aiApi, healthApi } from '../services/api'
import { useAppStore } from '../stores/useAppStore'

// Nutrition API Hooks
export const useNutritionSearch = () => {
  const addRecentSearch = useAppStore((state) => state.addRecentSearch)
  
  return useMutation({
    mutationFn: ({ foodName, weight = 100 }: { foodName: string; weight?: number }) => 
      nutritionApi.search(foodName, weight),
    onSuccess: (data, variables) => {
      if (data.success && data.nutrition) {
        addRecentSearch(variables.foodName)
      }
    },
    onError: (error) => {
      console.error('Nutrition search failed:', error)
    }
  })
}

export const useNutritionStatistics = () => {
  return useQuery({
    queryKey: ['nutrition', 'statistics'],
    queryFn: nutritionApi.getStatistics,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 1,
  })
}

export const useNutritionHealth = () => {
  return useQuery({
    queryKey: ['nutrition', 'health'],
    queryFn: nutritionApi.healthCheck,
    staleTime: 1 * 60 * 1000, // 1 minute
    retry: 2,
  })
}

export const useBatchNutritionSearch = () => {
  return useMutation({
    mutationFn: (foods: Array<{ name: string; weight?: number }>) => 
      nutritionApi.batchSearch(foods),
    onError: (error) => {
      console.error('Batch nutrition search failed:', error)
    }
  })
}

// Vision API Hooks
export const useImageAnalysis = () => {
  const addFoodDetection = useAppStore((state) => state.addFoodDetection)
  
  return useMutation({
    mutationFn: ({ imageFile, userId }: { imageFile: File; userId?: string }) => 
      visionApi.analyzeImage(imageFile, userId),
    onSuccess: (data) => {
      if (data.success && data.detection) {
        addFoodDetection({
          foodName: data.detection.foodName,
          confidence: data.detection.confidence,
          estimatedWeight: data.detection.estimatedWeight,
          calories: 0, // Will be calculated based on nutrition data
          timestamp: new Date().toISOString(),
        })
      }
    },
    onError: (error) => {
      console.error('Image analysis failed:', error)
    }
  })
}

export const useImageValidation = () => {
  return useMutation({
    mutationFn: (imageFile: File) => visionApi.validateImage(imageFile),
    onError: (error) => {
      console.error('Image validation failed:', error)
    }
  })
}

export const useVisionModelInfo = () => {
  return useQuery({
    queryKey: ['vision', 'model-info'],
    queryFn: visionApi.getModelInfo,
    staleTime: 10 * 60 * 1000, // 10 minutes
    retry: 1,
  })
}

export const useVisionHealth = () => {
  return useQuery({
    queryKey: ['vision', 'health'],
    queryFn: visionApi.healthCheck,
    staleTime: 1 * 60 * 1000, // 1 minute
    retry: 2,
  })
}

// AI API Hooks
export const useAIFoodAnalysis = () => {
  return useMutation({
    mutationFn: ({ content, userId }: { content: string; userId: string }) => 
      aiApi.analyzeFood(content, userId),
    onError: (error) => {
      console.error('AI food analysis failed:', error)
    }
  })
}

export const useAINutritionAdvice = () => {
  return useMutation({
    mutationFn: ({ content, userId }: { content: string; userId: string }) => 
      aiApi.getNutritionAdvice(content, userId),
    onError: (error) => {
      console.error('AI nutrition advice failed:', error)
    }
  })
}

export const useAIWorkoutPlan = () => {
  return useMutation({
    mutationFn: ({ content, userId }: { content: string; userId: string }) => 
      aiApi.createWorkoutPlan(content, userId),
    onError: (error) => {
      console.error('AI workout plan failed:', error)
    }
  })
}

export const useAIProgressAnalysis = () => {
  return useMutation({
    mutationFn: ({ content, userId }: { content: string; userId: string }) => 
      aiApi.analyzeProgress(content, userId),
    onError: (error) => {
      console.error('AI progress analysis failed:', error)
    }
  })
}

export const useAIComplexQuery = () => {
  return useMutation({
    mutationFn: ({ content, userId }: { content: string; userId: string }) => 
      aiApi.complexQuery(content, userId),
    onError: (error) => {
      console.error('AI complex query failed:', error)
    }
  })
}

export const useAIStatistics = () => {
  return useQuery({
    queryKey: ['ai', 'statistics'],
    queryFn: aiApi.getStatistics,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 1,
  })
}

// Health Check Hooks
export const useSystemHealth = () => {
  return useQuery({
    queryKey: ['system', 'health'],
    queryFn: healthApi.checkAll,
    staleTime: 30 * 1000, // 30 seconds
    retry: 2,
    refetchInterval: 60 * 1000, // Refetch every minute
  })
}

// Cache Management Hooks
export const useClearNutritionCache = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: nutritionApi.clearCache,
    onSuccess: () => {
      // Invalidate all nutrition queries
      queryClient.invalidateQueries({ queryKey: ['nutrition'] })
    },
    onError: (error) => {
      console.error('Clear nutrition cache failed:', error)
    }
  })
}

export const useClearAICache = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: aiApi.clearCache,
    onSuccess: () => {
      // Invalidate all AI queries
      queryClient.invalidateQueries({ queryKey: ['ai'] })
    },
    onError: (error) => {
      console.error('Clear AI cache failed:', error)
    }
  })
}

// Combined hooks for complex operations
export const useAnalyzeImageAndGetNutrition = () => {
  const imageAnalysis = useImageAnalysis()
  const nutritionSearch = useNutritionSearch()
  const addNutritionEntry = useAppStore((state) => state.addNutritionEntry)
  
  const analyzeComplete = async (imageFile: File, userId?: string) => {
    try {
      // First, analyze the image
      const visionResult = await imageAnalysis.mutateAsync({ imageFile, userId })
      
      if (!visionResult.success || !visionResult.detection) {
        throw new Error('Image analysis failed')
      }
      
      // Then, get nutrition info for the detected food
      const nutritionResult = await nutritionSearch.mutateAsync({
        foodName: visionResult.detection.foodName,
        weight: visionResult.detection.estimatedWeight
      })
      
      if (nutritionResult.success && nutritionResult.nutrition) {
        // Add to nutrition history
        addNutritionEntry({
          name: nutritionResult.nutrition.name,
          calories: nutritionResult.nutrition.calories,
          protein: nutritionResult.nutrition.macros.protein,
          carbohydrates: nutritionResult.nutrition.macros.carbohydrates,
          fat: nutritionResult.nutrition.macros.fat,
          weight: nutritionResult.nutrition.weight,
          date: new Date().toISOString(),
        })
      }
      
      return {
        vision: visionResult,
        nutrition: nutritionResult,
      }
    } catch (error) {
      console.error('Combined analysis failed:', error)
      throw error
    }
  }
  
  return {
    analyzeComplete,
    isLoading: imageAnalysis.isPending || nutritionSearch.isPending,
    error: imageAnalysis.error || nutritionSearch.error,
  }
}

// Hook for getting comprehensive stats
export const useComprehensiveStats = () => {
  const nutritionStats = useNutritionStatistics()
  const aiStats = useAIStatistics()
  const visionModelInfo = useVisionModelInfo()
  const systemHealth = useSystemHealth()
  
  return {
    nutrition: nutritionStats.data,
    ai: aiStats.data,
    vision: visionModelInfo.data,
    health: systemHealth.data,
    isLoading: nutritionStats.isLoading || aiStats.isLoading || visionModelInfo.isLoading,
    isError: nutritionStats.isError || aiStats.isError || visionModelInfo.isError,
    refetch: () => {
      nutritionStats.refetch()
      aiStats.refetch()
      visionModelInfo.refetch()
      systemHealth.refetch()
    }
  }
} 