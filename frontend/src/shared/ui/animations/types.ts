export interface BaseAnimationProps {
  text: string;
  className?: string;
  delay?: number;
  duration?: number;
  onComplete?: () => void;
}

export type AnimationDirection = 'top' | 'bottom' | 'left' | 'right';
export type SplitType = 'chars' | 'words' | 'lines';
export type GlitchIntensity = 'low' | 'medium' | 'high';
export type ShimmerDirection = 'left' | 'right' | 'top' | 'bottom';

export interface SplitTextProps extends BaseAnimationProps {
  splitType?: SplitType;
  stagger?: number;
  triggerOnScroll?: boolean;
  enableGradient?: boolean;
  use3D?: boolean;
  bounceEffect?: boolean;
}

export interface BlurTextProps extends BaseAnimationProps {
  direction?: AnimationDirection;
  distance?: number;
  blur?: number;
  triggerOnScroll?: boolean;
}

export interface GlitchTextProps extends BaseAnimationProps {
  intensity?: GlitchIntensity;
  colors?: string[];
  enableOnHover?: boolean;
  autoPlay?: boolean;
  speed?: number;
}

export interface ShinyTextProps extends BaseAnimationProps {
  shimmerDirection?: ShimmerDirection;
  shimmerWidth?: string;
  shimmerColor?: string;
  playOnHover?: boolean;
  autoPlay?: boolean;
  speed?: number;
}

export interface TextTypeProps extends BaseAnimationProps {
  typeSpeed?: number;
  deleteSpeed?: number;
  loop?: boolean;
  showCursor?: boolean;
  cursorChar?: string;
  words?: string[];
} 