'use client'

import { Star } from 'lucide-react'

const testimonials = [
  {
    name: 'Sarah Johnson',
    role: 'CTO at TechCorp',
    image: '👩‍💼',
    text: 'CyberGate has completely transformed our security infrastructure. The real-time threat detection is incredible.',
    rating: 5,
  },
  {
    name: 'Michael Chen',
    role: 'Security Lead at FinanceHub',
    image: '👨‍💼',
    text: 'Outstanding support team and incredibly reliable. We haven\'t had a single security incident since implementing CyberGate.',
    rating: 5,
  },
  {
    name: 'Emma Davis',
    role: 'CEO at StartupXYZ',
    image: '👩‍🔬',
    text: 'Best investment we made for our company. The ROI has been phenomenal and our customers love the improved security.',
    rating: 5,
  },
  {
    name: 'James Wilson',
    role: 'IT Manager at Enterprise Co',
    image: '👨‍💻',
    text: 'Easy to deploy, easy to manage, and incredibly effective. Highly recommended for any organization.',
    rating: 5,
  },
]

export function Testimonials() {
  return (
    <section id="testimonials" className="py-20 border-t border-border">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="text-center space-y-4 mb-16">
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold">
            Trusted by Industry Leaders
          </h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            See what our customers have to say about CyberGate
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {testimonials.map((testimonial) => (
            <div
              key={testimonial.name}
              className="p-6 rounded-lg border border-border bg-card hover:bg-secondary/50 transition-all duration-300 hover:border-primary/50"
            >
              {/* Rating */}
              <div className="flex gap-1 mb-4">
                {Array.from({ length: testimonial.rating }).map((_, i) => (
                  <Star
                    key={i}
                    className="h-4 w-4 fill-yellow-400 text-yellow-400"
                  />
                ))}
              </div>

              {/* Review Text */}
              <p className="text-foreground mb-4 leading-relaxed">
                "{testimonial.text}"
              </p>

              {/* Author */}
              <div className="flex items-center gap-3 pt-4 border-t border-border">
                <div className="text-3xl">{testimonial.image}</div>
                <div>
                  <p className="font-semibold">{testimonial.name}</p>
                  <p className="text-xs text-muted-foreground">{testimonial.role}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
