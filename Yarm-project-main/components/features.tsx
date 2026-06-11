'use client'

import { Shield, Zap, Lock, Code, BarChart3, Users } from 'lucide-react'

const features = [
  {
    icon: Shield,
    title: 'Advanced Protection',
    description: 'Military-grade encryption and advanced threat detection',
  },
  {
    icon: Zap,
    title: 'Lightning Fast',
    description: 'Sub-millisecond response times for critical threats',
  },
  {
    icon: Lock,
    title: 'Zero Trust Security',
    description: 'Never trust, always verify approach to security',
  },
  {
    icon: Code,
    title: 'API Integration',
    description: 'Easy-to-use APIs for seamless integration',
  },
  {
    icon: BarChart3,
    title: 'Real-time Analytics',
    description: 'Comprehensive dashboards with real-time insights',
  },
  {
    icon: Users,
    title: '24/7 Support',
    description: 'Expert support team always ready to help',
  },
]

export function Features() {
  return (
    <section id="features" className="py-20 border-t border-border">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="text-center space-y-4 mb-16">
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold">
            Powerful Features
          </h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Everything you need to secure your infrastructure
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature) => {
            const Icon = feature.icon
            return (
              <div
                key={feature.title}
                className="group p-6 rounded-lg border border-border bg-card hover:bg-secondary/50 transition-all duration-300 hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10"
              >
                <div className="inline-flex p-3 rounded-lg bg-primary/10 text-primary group-hover:bg-primary/20 transition-colors mb-4">
                  <Icon className="h-6 w-6" />
                </div>
                <h3 className="text-lg font-semibold mb-2 group-hover:text-primary transition-colors">
                  {feature.title}
                </h3>
                <p className="text-muted-foreground group-hover:text-foreground transition-colors">
                  {feature.description}
                </p>
              </div>
            )
          })}
        </div>
      </div>
    </section>
  )
}
