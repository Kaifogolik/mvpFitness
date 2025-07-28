import React, { useState, useEffect, useRef } from 'react';
import { cn } from '../../utils/cn';
import type { TextTypeProps } from './types';

const TextType: React.FC<TextTypeProps> = ({
  text,
  texts = [text],
  className,
  typeSpeed = 100,
  deleteSpeed = 50,
  pauseBetween = 2000,
  loop = true,
  showCursor = true,
  cursorChar = '|',
  cursorBlink = true,
  onComplete,
  ...props
}) => {
  const [displayText, setDisplayText] = useState('');
  const [currentTextIndex, setCurrentTextIndex] = useState(0);
  const [isTyping, setIsTyping] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    const currentText = texts[currentTextIndex] || '';
    
    const type = () => {
      if (isDeleting) {
        // Удаляем символы
        if (displayText.length > 0) {
          setDisplayText(prev => prev.slice(0, -1));
          timeoutRef.current = setTimeout(type, deleteSpeed);
        } else {
          // Переходим к следующему тексту
          setIsDeleting(false);
          setCurrentTextIndex(prev => {
            const nextIndex = (prev + 1) % texts.length;
            if (nextIndex === 0 && !loop) {
              setIsTyping(false);
              if (onComplete) onComplete();
              return prev;
            }
            return nextIndex;
          });
        }
      } else {
        // Добавляем символы
        if (displayText.length < currentText.length) {
          setDisplayText(prev => currentText.slice(0, prev.length + 1));
          timeoutRef.current = setTimeout(type, typeSpeed);
        } else {
          // Пауза перед удалением (если есть несколько текстов)
          if (texts.length > 1) {
            timeoutRef.current = setTimeout(() => {
              setIsDeleting(true);
              type();
            }, pauseBetween);
          } else if (!loop) {
            setIsTyping(false);
            if (onComplete) onComplete();
          }
        }
      }
    };

    if (isTyping) {
      timeoutRef.current = setTimeout(type, typeSpeed);
    }

    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, [
    displayText,
    currentTextIndex,
    texts,
    typeSpeed,
    deleteSpeed,
    pauseBetween,
    loop,
    isTyping,
    isDeleting,
    onComplete
  ]);

  return (
    <span
      className={cn('inline-block', className)}
      {...props}
    >
      <span>{displayText}</span>
      {showCursor && (
        <span
          className={cn(
            'ml-1',
            cursorBlink && 'animate-pulse'
          )}
          style={{
            animationDuration: '1s',
          }}
        >
          {cursorChar}
        </span>
      )}
    </span>
  );
};

export { TextType };
export type { TextTypeProps }; 