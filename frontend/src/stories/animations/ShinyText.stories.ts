import type { Meta, StoryObj } from '@storybook/react';
import { ShinyText } from '../../shared/ui/animations/ShinyText';

const meta: Meta<typeof ShinyText> = {
  title: 'Animations/ShinyText',
  component: ShinyText,
  parameters: {
    layout: 'centered',
    docs: {
      description: {
        component: 'ShinyText создает эффект бегущего блика по тексту, как на премиальных логотипах.',
      },
    },
  },
  tags: ['autodocs'],
  argTypes: {
    text: {
      control: 'text',
      description: 'Текст для анимации',
    },
    speed: {
      control: { type: 'number', min: 1, max: 10, step: 0.5 },
      description: 'Скорость анимации (секунды)',
    },
    direction: {
      control: 'select',
      options: ['left-to-right', 'right-to-left'],
      description: 'Направление блика',
    },
    shimmerWidth: {
      control: { type: 'number', min: 50, max: 200, step: 10 },
      description: 'Ширина блика (%)',
    },
    autoPlay: {
      control: 'boolean',
      description: 'Автоматический запуск',
    },
    playOnHover: {
      control: 'boolean',
      description: 'Запускать при наведении',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    text: 'SHINY TEXT',
    speed: 3,
    direction: 'left-to-right',
    shimmerWidth: 100,
    autoPlay: true,
    playOnHover: false,
    className: 'text-4xl font-bold text-gradient-primary',
  },
};

export const OnHover: Story = {
  args: {
    text: 'Наведи курсор',
    speed: 2,
    direction: 'left-to-right',
    shimmerWidth: 80,
    autoPlay: false,
    playOnHover: true,
    className: 'text-3xl font-bold text-neutral-800 cursor-pointer',
  },
  parameters: {
    docs: {
      description: {
        story: 'Блик появляется только при наведении курсора.',
      },
    },
  },
};

export const FastShimmer: Story = {
  args: {
    text: 'Быстрый блик',
    speed: 1,
    direction: 'left-to-right',
    shimmerWidth: 60,
    autoPlay: true,
    playOnHover: false,
    className: 'text-3xl font-bold text-secondary-600',
  },
};

export const SlowShimmer: Story = {
  args: {
    text: 'Медленный блик',
    speed: 6,
    direction: 'right-to-left',
    shimmerWidth: 120,
    autoPlay: true,
    playOnHover: false,
    className: 'text-3xl font-bold text-primary-600',
  },
};

export const CustomColor: Story = {
  args: {
    text: 'GOLDEN SHINE',
    speed: 4,
    direction: 'left-to-right',
    shimmerWidth: 100,
    color: 'rgba(255, 215, 0, 0.8)',
    autoPlay: true,
    playOnHover: false,
    className: 'text-4xl font-bold text-amber-800',
  },
  parameters: {
    docs: {
      description: {
        story: 'Блик с кастомным золотым цветом.',
      },
    },
  },
}; 