import React, { useState, useEffect } from 'react';
import { cn } from '../../utils/cn';
import type { TextTypeProps } from './types';

const TextType: React.FC<TextTypeProps> = ({
  text,
  className,
  words = [text],
  typeSpeed = 100,
  deleteSpeed = 50,
  loop = true,
  showCursor = true,
  cursorChar = '|',
  delay = 0,
  duration = 2,
  onComplete,
  ...props
}) => {
  const [displayText, setDisplayText] = useState('');
  const [textIndex, setTextIndex] = useState(0);
  const [charIndex, setCharIndex] = useState(0);
  const [isDeleting, setIsDeleting] = useState(false);
  const [showCursorState, setShowCursorState] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (words.length === 0) return;

      const type = () => {
        const fullText = words[textIndex] || '';

        if (!isDeleting) {
          if (charIndex < fullText.length) {
            setDisplayText(fullText.substring(0, charIndex + 1));
            setCharIndex(charIndex + 1);
          } else {
            setTimeout(() => setIsDeleting(true), 1000);
          }
        } else {
          if (charIndex > 0) {
            setDisplayText(fullText.substring(0, charIndex - 1));
            setCharIndex(charIndex - 1);
          } else {
            setIsDeleting(false);
            setTextIndex((prev) => (prev + 1) % words.length);
            if (!loop && textIndex === words.length - 1) {
              if (onComplete) onComplete();
              return;
            }
          }
        }
      };

      type();
    }, delay);

    return () => clearTimeout(timer);
  }, [charIndex, isDeleting, textIndex, words, typeSpeed, deleteSpeed, loop, delay, onComplete]);

  useEffect(() => {
    if (showCursor) {
      const cursorTimer = setInterval(() => {
        setShowCursorState(prev => !prev);
      }, 500);
      return () => clearInterval(cursorTimer);
    }
  }, [showCursor]);

  useEffect(() => {
    const timer = setTimeout(() => {
      // Start typing
    }, delay);
    return () => clearTimeout(timer);
  }, [delay]);

  return (
    <span className={cn('inline-block', className)} {...props}>
      {displayText}
      {showCursor && (
        <span
          className={cn(
            'inline-block ml-1',
            showCursorState ? 'opacity-100' : 'opacity-0'
          )}
        >
          {cursorChar}
        </span>
      )}
    </span>
  );
};

export default TextType;
export type { TextTypeProps }; 