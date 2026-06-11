'use client'

import { ArrowRight, Mail } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { useState } from 'react'

interface CTAProps {
  onNotify: (message: string, type: 'success' | 'error' | 'info') => void
}

export function CTA({ onNotify }: CTAProps) {
  const [email, setEmail] = useState('')

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!email) {
      onNotify('Please enter a valid email', 'error')
      return
    }
    onNotify('Thanks for subscribing! Check your email for updates.', 'success')
    setEmail('')
  }

  return (
    <section className="py-20 border-t border-border">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="relative rounded-lg border border-border overflow-hidden">
          {/* Background gradient */}
          <div className="absolute inset-0 -z-10">
            <div className="absolute top-0 right-0 -translate-y-1/2 translate-x-1/2 w-96 h-96 bg-gradient-to-br from-blue-500/20 to-purple-600/20 rounded-full blur-3xl" />
          </div>

          <div className="p-8 sm:p-12 md:p-16">
            <div className="max-w-2xl mx-auto text-center space-y-8">
              <div className="space-y-4">
                <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold">
                  Ready to secure your infrastructure?
                </h2>
                <p className="text-lg text-muted-foreground">
                  Join thousands of companies protecting their systems with CyberGate
                </p>
              </div>

              {/* Email Subscription */}
              <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-3 max-w-md mx-auto">
                <div className="relative flex-1">
                  <Input
                    type="email"
                    placeholder="Enter your email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="pl-10"
                  />
                  <Mail className="absolute left-3 top-3 h-5 w-5 text-muted-foreground" />
                </div>
                <Button
                  type="submit"
                  className="gap-2 bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 whitespace-nowrap"
                >
                  Subscribe
                  <ArrowRight className="h-4 w-4" />
                </Button>
              </form>

              {/* Additional CTA */}
              <Button size="lg" className="gap-2">
                Start Free Trial
                <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
