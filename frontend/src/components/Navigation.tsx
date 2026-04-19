'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const navItems = [
  { href: '/releases', label: 'Releases' },
  { href: '/environments', label: 'Environments' },
  { href: '/components', label: 'Components' },
];

export default function Navigation() {
  const pathname = usePathname();

  return (
    <nav className="bg-brand-navy text-white">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded bg-brand-yellow" />
            <span className="text-lg font-bold tracking-tight">Release Management</span>
          </div>
          <div className="flex gap-1">
            {navItems.map((item) => {
              const active = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                    active
                      ? 'bg-brand-blue text-white'
                      : 'text-white/80 hover:bg-white/10 hover:text-white'
                  }`}
                >
                  {item.label}
                </Link>
              );
            })}
          </div>
        </div>
      </div>
    </nav>
  );
}
