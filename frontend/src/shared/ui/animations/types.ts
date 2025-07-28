export interface BaseAnimationProps {
  text: string;
  className?: string;
  delay?: number;
  duration?: number;
  onComplete?: () => void;
}

export interface SplitTextProps extends BaseAnimationProps {
  splitType?: 'chars' | 'words' | 'lines';
  ease?: string;
  threshold?: number;
  stagger?: number;
  triggerOnScroll?: boolean;
}

export interface BlurTextProps extends BaseAnimationProps {
  direction?: 'top' | 'bottom' | 'left' | 'right';
  distance?: number;
  blur?: number;
  triggerOnScroll?: boolean;
}

export interface GlitchTextProps extends BaseAnimationProps {
  enableOnHover?: boolean;
  glitchIntensity?: 'low' | 'medium' | 'high';
  colors?: {
    primary: string;
    secondary: string;
  };
  autoPlay?: boolean;
  playDuration?: number;
}

export interface ShinyTextProps extends BaseAnimationProps {
  speed?: number;
  direction?: 'left-to-right' | 'right-to-left';
  shimmerWidth?: number;
  color?: string;
  autoPlay?: boolean;
  playOnHover?: boolean;
}

export interface TextTypeProps extends BaseAnimationProps {
  texts?: string[];
  typeSpeed?: number;
  deleteSpeed?: number;
  pauseBetween?: number;
  loop?: boolean;
  showCursor?: boolean;
  cursorChar?: string;
  cursorBlink?: boolean;
}

export type AnimationDirection = 'top' | 'bottom' | 'left' | 'right';
export type SplitType = 'chars' | 'words' | 'lines';
export type GlitchIntensity = 'low' | 'medium' | 'high';
export type ShimmerDirection = 'left-to-right' | 'right-to-left'; 