import type { Meta, StoryObj } from '@storybook/react';
import { GlitchText } from '../../shared/ui/animations/GlitchText';

const meta: Meta<typeof GlitchText> = {
  title: 'Animations/GlitchText',
  component: GlitchText,
  parameters: {
    layout: 'centered',
    docs: {
      description: {
        component: 'GlitchText создает цифровой глитч эффект с цветными тенями и дрожанием текста.',
      },
    },
  },
  tags: ['autodocs'],
  argTypes: {
    text: {
      control: 'text',
      description: 'Текст для анимации',
    },
    enableOnHover: {
      control: 'boolean',
      description: 'Запускать эффект при наведении',
    },
    glitchIntensity: {
      control: 'select',
      options: ['low', 'medium', 'high'],
      description: 'Интенсивность глитча',
    },
    autoPlay: {
      control: 'boolean',
      description: 'Автоматический запуск эффекта',
    },
    playDuration: {
      control: { type: 'number', min: 1000, max: 10000, step: 500 },
      description: 'Интервал автоповтора (мс)',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    text: 'GLITCH EFFECT',
    enableOnHover: false,
    glitchIntensity: 'medium',
    autoPlay: true,
    playDuration: 3000,
    className: 'text-4xl font-bold text-white bg-black px-4 py-2',
  },
};

export const OnHover: Story = {
  args: {
    text: 'Наведи курсор',
    enableOnHover: true,
    glitchIntensity: 'medium',
    autoPlay: false,
    className: 'text-3xl font-bold text-primary-600 cursor-pointer',
  },
  parameters: {
    docs: {
      description: {
        story: 'Глитч эффект активируется при наведении курсора.',
      },
    },
  },
};

export const LowIntensity: Story = {
  args: {
    text: 'Легкий глитч',
    enableOnHover: false,
    glitchIntensity: 'low',
    autoPlay: true,
    playDuration: 4000,
    className: 'text-3xl font-bold text-neutral-800',
  },
};

export const HighIntensity: Story = {
  args: {
    text: 'МОЩНЫЙ ГЛИТЧ',
    enableOnHover: false,
    glitchIntensity: 'high',
    autoPlay: true,
    playDuration: 2000,
    className: 'text-4xl font-black text-red-600',
  },
};

export const CustomColors: Story = {
  args: {
    text: 'CUSTOM COLORS',
    enableOnHover: true,
    glitchIntensity: 'medium',
    autoPlay: false,
    colors: {
      primary: '#00FF00',
      secondary: '#FF00FF',
    },
    className: 'text-3xl font-bold text-blue-600',
  },
  parameters: {
    docs: {
      description: {
        story: 'Глитч с кастомными цветами теней.',
      },
    },
  },
}; 