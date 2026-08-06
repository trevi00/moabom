'use client';

import { useEffect, useState } from 'react';

export default function HealthProbe() {
  const [health, setHealth] = useState<string>('loading…');

  useEffect(() => {
    fetch('/api/health')
      .then((r) => (r.ok ? r.json() : Promise.reject(new Error(`HTTP ${r.status}`))))
      .then((d) => setHealth(JSON.stringify(d)))
      .catch((e) => setHealth(`ERROR: ${String(e)}`));
  }, []);

  return <pre data-testid="health">{health}</pre>;
}
