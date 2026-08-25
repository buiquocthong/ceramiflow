import { useCallback, useState } from 'react'

const STORAGE_KEY = 'ceramiflow.operator'

export function useOperator() {
  const [operator, setOperatorState] = useState(() => localStorage.getItem(STORAGE_KEY) || 'Thong')
  const setOperator = useCallback((value: string) => {
    const next = value.trim() || 'system'
    setOperatorState(next)
    localStorage.setItem(STORAGE_KEY, next)
  }, [])
  return { operator, setOperator }
}
