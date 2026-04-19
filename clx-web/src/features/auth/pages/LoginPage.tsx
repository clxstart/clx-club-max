import React, { useMemo } from 'react';
import { LoginForm } from '../components/LoginForm';

// 静态配置 - 保持合理的元素数量
const FLOATING_ORBS = [
  { size: 300, color: '#39ff14', top: '10%', left: '10%', delay: '0s' },
  { size: 250, color: '#a020f0', top: '60%', right: '5%', delay: '-5s' },
  { size: 200, color: '#ff006e', bottom: '10%', left: '30%', delay: '-10s' },
];

const GLOW_CIRCLES = [
  { size: 180, type: 'green', top: '5%', left: '5%', delay: '0s' },
  { size: 220, type: 'purple', top: '15%', right: '10%', delay: '-3s' },
  { size: 150, type: 'pink', bottom: '20%', left: '8%', delay: '-6s' },
];

// 旋转方块
const ROTATING_SQUARES = [
  { size: 16, color: '#39ff14', top: '12%', left: '8%', delay: '0s' },
  { size: 12, color: '#a020f0', top: '70%', right: '12%', delay: '-2s' },
  { size: 14, color: '#ff006e', bottom: '25%', left: '15%', delay: '-4s' },
];

// 浮动三角形
const FLOATING_TRIANGLES = [
  { color: '#39ff14', top: '25%', left: '18%', delay: '0s' },
  { color: '#a020f0', top: '55%', right: '15%', delay: '-2s' },
];

// 脉冲圆点
const PULSE_DOTS = [
  { color: '#39ff14', top: '20%', left: '35%', delay: '0s' },
  { color: '#ff006e', bottom: '30%', right: '20%', delay: '-1s' },
  { color: '#a020f0', top: '65%', left: '10%', delay: '-0.5s' },
];

// 跳动方块
const BOUNCING_BLOCKS = [
  { color: '#39ff14', top: '35%', left: '5%', delay: '0s' },
  { color: '#20c9e0', bottom: '15%', right: '8%', delay: '-1s' },
];

// 飘动弧线
const ARC_FLOATS = [
  { color: '#39ff14', top: '40%', left: '12%', delay: '0s' },
  { color: '#ff006e', bottom: '35%', right: '10%', delay: '-2s' },
];

// 心跳小点
const HEARTBEAT_DOTS = [
  { color: '#ff006e', top: '28%', right: '25%', delay: '0s' },
  { color: '#39ff14', bottom: '22%', left: '20%', delay: '-0.5s' },
];

// 闪烁星芒
const STAR_BURSTS = [
  { color: '#39ff14', top: '8%', left: '25%' },
  { color: '#a020f0', bottom: '12%', right: '30%' },
  { color: '#ff006e', top: '75%', left: '8%' },
];

// 十字光标
const CROSS_GLOWS = [
  { color: '#39ff14', top: '15%', left: '45%', size: 'text-xl' },
  { color: '#a020f0', bottom: '18%', right: '35%', size: 'text-lg' },
];

const SPARKLES = 12; // 闪烁点数量

/**
 * 登录页面 - 现代赛博风格（优化性能）
 */
export const LoginPage: React.FC = () => {
  // 生成闪烁点
  const sparkles = useMemo(() =>
    Array.from({ length: SPARKLES }, (_, i) => ({
      id: i,
      left: `${Math.random() * 100}%`,
      top: `${Math.random() * 100}%`,
      delay: `${Math.random() * 3}s`,
      color: ['green', 'purple', 'pink', 'cyan'][i % 4] as 'green' | 'purple' | 'pink' | 'cyan',
    })),
  []);

  return (
    <div className="min-h-screen bg-[#0a0a0a] relative overflow-hidden flex items-center justify-center">

      {/* 扫描线效果 */}
      <div className="scan-line bg-gradient-to-r from-transparent via-[#39ff14]/20 to-transparent" />

      {/* 浮动光球背景 */}
      {FLOATING_ORBS.map((orb, i) => (
        <div
          key={`orb-${i}`}
          className="floating-orb"
          style={{
            width: orb.size,
            height: orb.size,
            background: orb.color,
            top: orb.top,
            left: orb.left,
            right: orb.right,
            bottom: orb.bottom,
            animationDelay: orb.delay,
          }}
        />
      ))}

      {/* 发光圆圈装饰 */}
      {GLOW_CIRCLES.map((circle, i) => (
        <div
          key={`circle-${i}`}
          className={`glow-circle glow-circle-${circle.type}`}
          style={{
            width: circle.size,
            height: circle.size,
            top: circle.top,
            left: circle.left,
            right: circle.right,
            bottom: circle.bottom,
            animationDelay: circle.delay,
          }}
        />
      ))}

      {/* 旋转方块 */}
      {ROTATING_SQUARES.map((sq, i) => (
        <div
          key={`sq-${i}`}
          className="rotating-square"
          style={{
            width: sq.size,
            height: sq.size,
            borderColor: sq.color,
            top: sq.top,
            left: sq.left,
            right: sq.right,
            animationDelay: sq.delay,
          }}
        />
      ))}

      {/* 浮动三角形 */}
      {FLOATING_TRIANGLES.map((tri, i) => (
        <div
          key={`tri-${i}`}
          className="floating-triangle"
          style={{
            borderBottomColor: tri.color,
            top: tri.top,
            left: tri.left,
            right: tri.right,
            animationDelay: tri.delay,
          }}
        />
      ))}

      {/* 脉冲圆点 */}
      {PULSE_DOTS.map((dot, i) => (
        <div
          key={`pd-${i}`}
          className="pulse-dot"
          style={{
            background: dot.color,
            top: dot.top,
            left: dot.left,
            right: dot.right,
            animationDelay: dot.delay,
          }}
        />
      ))}

      {/* 跳动方块 */}
      {BOUNCING_BLOCKS.map((block, i) => (
        <div
          key={`bb-${i}`}
          className="bouncing-block"
          style={{
            background: block.color,
            top: block.top,
            left: block.left,
            right: block.right,
            animationDelay: block.delay,
          }}
        />
      ))}

      {/* 飘动弧线 */}
      {ARC_FLOATS.map((arc, i) => (
        <div
          key={`arc-${i}`}
          className="arc-float"
          style={{
            color: arc.color,
            top: arc.top,
            left: arc.left,
            right: arc.right,
            animationDelay: arc.delay,
          }}
        />
      ))}

      {/* 心跳小点 */}
      {HEARTBEAT_DOTS.map((hb, i) => (
        <div
          key={`hb-${i}`}
          className="heartbeat-dot"
          style={{
            background: hb.color,
            top: hb.top,
            left: hb.left,
            right: hb.right,
            animationDelay: hb.delay,
          }}
        />
      ))}

      {/* 闪烁星芒 */}
      {STAR_BURSTS.map((star, i) => (
        <div
          key={`star-${i}`}
          className="star-burst text-sm"
          style={{
            color: star.color,
            top: star.top,
            left: star.left,
            right: star.right,
          }}
        >
          ✦
        </div>
      ))}

      {/* 十字光标 */}
      {CROSS_GLOWS.map((cross, i) => (
        <div
          key={`cross-${i}`}
          className={`cross-glow ${cross.size}`}
          style={{
            color: cross.color,
            top: cross.top,
            left: cross.left,
            right: cross.right,
          }}
        >
          +
        </div>
      ))}

      {/* 环形进度装饰 */}
      <div className="ring-progress w-[60px] h-[60px]" style={{ top: '10%', right: '22%' }} />
      <div className="ring-progress w-[40px] h-[40px]" style={{ bottom: '15%', left: '18%', animationDelay: '-2s' }} />

      {/* 闪烁点 */}
      {sparkles.map((s) => (
        <div
          key={s.id}
          className={`sparkle sparkle-${s.color}`}
          style={{ left: s.left, top: s.top, animationDelay: s.delay }}
        />
      ))}

      {/* 主内容 */}
      <div className="relative z-10 w-full max-w-md px-4">

        {/* Logo 区 */}
        <div className="text-center mb-8 fade-in">
          <div className="mb-4">
            <div className="w-16 h-16 mx-auto rounded-xl bg-gradient-to-br from-[#39ff14] to-[#20c9e0] flex items-center justify-center logo-glow">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#0a0a0a" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="12 2 2 7 12 12 22 7 12 2" />
                <polyline points="2 17 12 22 22 17" />
                <polyline points="2 12 12 17 22 12" />
              </svg>
            </div>
          </div>

          <h1 className="text-3xl md:text-4xl font-black uppercase tracking-widest text-[#39ff14] neon-text mb-2">
            CLXHXH
          </h1>

          <div className="divider-gradient mt-6" />
        </div>

        {/* 登录表单 */}
        <LoginForm />

      </div>

      {/* 底部装饰 */}
      <div className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-[#39ff14]/20 to-transparent" />
    </div>
  );
};

export default LoginPage;