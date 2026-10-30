import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export function Input({
  label,
  error,
  helperText,
  className = '',
  id,
  ...props
}: InputProps) {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full">
      {label && (
        <label htmlFor={inputId} className="block text-xs font-medium text-slate-300 mb-1.5">
          {label}
        </label>
      )}
      <input
        id={inputId}
        className={`w-full px-3.5 py-2.5 rounded-xl bg-brand-dark-card border ${
          error ? 'border-brand-crimson focus:border-brand-crimson' : 'border-brand-dark-border focus:border-brand-primary'
        } text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-1 focus:ring-brand-primary transition duration-150 ${className}`}
        {...props}
      />
      {error && <p className="mt-1 text-xs text-brand-crimson">{error}</p>}
      {helperText && !error && <p className="mt-1 text-xs text-slate-400">{helperText}</p>}
    </div>
  );
}
