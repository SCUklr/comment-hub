import { useState } from 'react';
import { DEMO_USERS } from '../utils/constants';

interface Props {
  userId: number;
  onChange: (id: number) => void;
}

export default function UserSwitcher({ userId, onChange }: Props) {
  const [customId, setCustomId] = useState('');

  const apply = (id: number) => {
    if (!id) return;
    onChange(id);
    setCustomId('');
  };

  return (
    <div className="user-switcher">
      <span>当前用户：</span>
      <select value={userId} onChange={(e) => apply(Number(e.target.value))}>
        {DEMO_USERS.map((u) => (
          <option key={u.id} value={u.id}>
            {u.name} ({u.id})
          </option>
        ))}
      </select>
      <input
        className="input"
        style={{ width: 110 }}
        placeholder="自定义ID"
        value={customId}
        onChange={(e) => setCustomId(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && apply(Number(customId))}
      />
      <button className="btn sm" onClick={() => apply(Number(customId))} disabled={!customId}>
        切换
      </button>
    </div>
  );
}
