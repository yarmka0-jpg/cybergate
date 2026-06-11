'use client'

import { useState } from 'react'
import { Header } from '@/components/header'
import { Hero } from '@/components/hero'
import { Features } from '@/components/features'
import { Services } from '@/components/services'
import { Testimonials } from '@/components/testimonials'
import { CTA } from '@/components/cta'
import { Footer } from '@/components/footer'
import { Toast } from '@/components/toast'

export default function Page() {
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <Header />
      <Hero onNotify={showToast} />
      <Features />
      <Services />
      <Testimonials />
      <CTA onNotify={showToast} />
      <Footer />
      {toast && <Toast message={toast.message} type={toast.type} />}
    </div>
  )
}
