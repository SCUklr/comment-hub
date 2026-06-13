import { createContext, useContext } from 'react';

export interface UserContextType {
  userId: number;
  setUserId: (id: number) => void;
}

export const UserContext = createContext<UserContextType>({
  userId: 1,
  setUserId: () => {},
});

export function useUser() {
  return useContext(UserContext);
}
