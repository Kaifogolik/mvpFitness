import type { Meta, StoryObj } from '@storybook/react';
import { SplitText } from '../../shared/ui/animations/SplitText';

const meta: Meta<typeof SplitText> = {
  title: 'Animations/SplitText',
  component: SplitText,
  parameters: {
    layout: 'centered',
    docs: {
      description: {
        component: 'SplitText анимирует появление текста по буквам, словам или строкам с эффектом 3D вращения.',
      },
    },
  },
  tags: ['autodocs'],
  argTypes: {
    text: {
      control: 'text',
      description: 'Текст для анимации',
    },
    splitType: {
      control: 'select',
      options: ['chars', 'words', 'lines'],
      description: 'Тип разделения текста',
    },
    delay: {
      control: { type: 'number', min: 0, max: 2000, step: 100 },
      description: 'Задержка перед началом анимации (мс)',
    },
    duration: {
      control: { type: 'number', min: 0.1, max: 2, step: 0.1 },
      description: 'Длительность анимации одного элемента',
    },
    stagger: {
      control: { type: 'number', min: 0.01, max: 0.3, step: 0.01 },
      description: 'Задержка между элементами',
    },
    triggerOnScroll: {
      control: 'boolean',
      description: 'Запускать анимацию при скролле',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    text: 'mvpFitness',
    splitType: 'chars',
    delay: 0,
    duration: 0.5,
    stagger: 0.05,
    triggerOnScroll: false,
    className: 'text-6xl font-bold text-gradient-primary',
  },
};

export const Words: Story = {
  args: {
    text: 'Революционная AI платформа для фитнеса',
    splitType: 'words',
    delay: 0,
    duration: 0.6,
    stagger: 0.1,
    triggerOnScroll: false,
    className: 'text-3xl font-semibold text-neutral-700',
  },
};

export const FastAnimation: Story = {
  args: {
    text: 'Быстрая анимация',
    splitType: 'chars',
    delay: 0,
    duration: 0.3,
    stagger: 0.02,
    triggerOnScroll: false,
    className: 'text-4xl font-bold text-secondary-600',
  },
};

export const SlowAnimation: Story = {
  args: {
    text: 'Медленная анимация',
    splitType: 'chars',
    delay: 0,
    duration: 0.8,
    stagger: 0.15,
    triggerOnScroll: false,
    className: 'text-4xl font-bold text-primary-600',
  },
};

export const WithScrollTrigger: Story = {
  args: {
    text: 'Анимация при скролле',
    splitType: 'chars',
    delay: 200,
    duration: 0.5,
    stagger: 0.05,
    triggerOnScroll: true,
    className: 'text-4xl font-bold text-gradient-primary',
  },
  parameters: {
    docs: {
      description: {
        story: 'Этот вариант анимируется при появлении в зоне видимости.',
      },
    },
  },
}; 