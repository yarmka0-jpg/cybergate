'use client'

import { CheckCircle2 } from 'lucide-react'
import { Button } from '@/components/ui/button'

const services = [
  {
    name: 'Starter',
    price: '$29',
    description: 'Perfect for small teams',
    features: [
      'Up to 5 team members',
      'Basic threat detection',
      'Email support',
      'API access',
      '5GB storage',
    ],
    popular: false,
  },
  {
    name: 'Professional',
    price: '$99',
    description: 'For growing businesses',
    features: [
      'Unlimited team members',
      'Advanced threat detection',
      'Priority support',
      'Full API access',
      'Unlimited storage',
      'Custom integrations',
    ],
    popular: true,
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    description: 'For large organizations',
    features: [
      'Dedicated support team',
      'Custom threat models',
      '24/7 phone support',
      'On-premise option',
      'SLA guarantee',
      'Custom contracts',
    ],
    popular: false,
  },
]

export function Services() {
  return (
    <section id="services" className="py-20 border-t border-border">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="text-center space-y-4 mb-16">
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold">
            Simple, Transparent Pricing
          </h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Choose the perfect plan for your needs
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {services.map((service) => (
            <div
              key={service.name}
              className={`relative rounded-lg border transition-all duration-300 ${
                service.popular
                  ? 'border-primary bg-primary/5 shadow-lg shadow-primary/20 scale-105'
                  : 'border-border bg-card hover:border-primary/50'
              }`}
            >
              {service.popular && (
                <div className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2">
                  <span className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-4 py-1 rounded-full text-xs font-semibold">
                    Most Popular
                  </span>
                </div>
              )}

              <div className="p-8">
                <h3 className="text-2xl font-bold mb-2">{service.name}</h3>
                <p className="text-muted-foreground mb-4">{service.description}</p>

                <div className="mb-6">
                  <span className="text-4xl font-bold">{service.price}</span>
                  {service.price !== 'Custom' && (
                    <span className="text-muted-foreground">/month</span>
                  )}
                </div>

                <Button
                  className={`w-full mb-8 ${
                    service.popular
                      ? 'bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700'
                      : ''
                  }`}
                  variant={service.popular ? 'default' : 'outline'}
                >
                  Get Started
                </Button>

                <div className="space-y-3">
                  {service.features.map((feature) => (
                    <div key={feature} className="flex gap-3 items-start">
                      <CheckCircle2 className="h-5 w-5 text-primary flex-shrink-0 mt-0.5" />
                      <span className="text-sm text-foreground">{feature}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
