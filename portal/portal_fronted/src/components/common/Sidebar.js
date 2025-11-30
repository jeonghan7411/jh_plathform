import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './Sidebar.css';

const Sidebar = ({ onToggle }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isCollapsed, setIsCollapsed] = useState(false);

  useEffect(() => {
    if (onToggle) {
      onToggle(isCollapsed);
    }
  }, [isCollapsed, onToggle]);

  // 메뉴 항목 정의
  const menuItems = [
    {
      id: 'dashboard',
      label: '대시보드',
      icon: '📊',
      path: '/dashboard',
    },
    {
      id: 'projects',
      label: '프로젝트',
      icon: '📦',
      path: '/dashboard/projects',
    },
    {
      id: 'settings',
      label: '설정',
      icon: '⚙️',
      path: '/dashboard/settings',
    },
  ];

  const handleMenuClick = (path) => {
    navigate(path);
  };

  const toggleSidebar = () => {
    setIsCollapsed(!isCollapsed);
  };

  return (
    <aside className={`sidebar ${isCollapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-header">
        {!isCollapsed && <h2 className="sidebar-title">메뉴</h2>}
        <button className="sidebar-toggle" onClick={toggleSidebar}>
          {isCollapsed ? '→' : '←'}
        </button>
      </div>

      <nav className="sidebar-nav">
        <ul className="menu-list">
          {menuItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <li key={item.id} className="menu-item">
                <button
                  className={`menu-button ${isActive ? 'active' : ''}`}
                  onClick={() => handleMenuClick(item.path)}
                  title={isCollapsed ? item.label : ''}
                >
                  <span className="menu-icon">{item.icon}</span>
                  {!isCollapsed && <span className="menu-label">{item.label}</span>}
                </button>
              </li>
            );
          })}
        </ul>
      </nav>
    </aside>
  );
};

export default Sidebar;

