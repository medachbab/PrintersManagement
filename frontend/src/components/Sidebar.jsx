import React, { useState, useEffect } from 'react';

const Sidebar = ({ setView, handleModelFilter, onCollapseChange }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [isCollapsed, setIsCollapsed] = useState(false);

  useEffect(() => {
    if (onCollapseChange) {
      onCollapseChange(isCollapsed);
    }
  }, [isCollapsed, onCollapseChange]);

  const navigationItems = [
    { 
      label: 'Home', 
      view: 'Home', 
      icon: (
        <svg className="w-5 h-5 text-gray-500 transition duration-75 group-hover:text-blue-600" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 22 21">
          <path d="M16.975 11H10V4.025a1 1 0 0 0-1.066-.998 8.5 8.5 0 1 0 9.039 9.039.999.999 0 0 0-1-1.066h.002Z"/>
          <path d="M12.5 0c-.157 0-.311.01-.565.027A1 1 0 0 0 11 1.02V10h8.975a1 1 0 0 0 1-.935c.013-.188.028-.374.028-.565A8.51 8.51 0 0 0 12.5 0Z"/>
        </svg>
      )
    },
    { 
      label: 'All Printers', 
      view: 'table', 
      icon: (
        <svg className="w-5 h-5 text-gray-500 transition duration-75 group-hover:text-blue-600" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 18 18">
          <path d="M6.143 0H1.857A1.857 1.857 0 0 0 0 1.857v4.286C0 7.169.831 8 1.857 8h4.286A1.857 1.857 0 0 0 8 6.143V1.857A1.857 1.857 0 0 0 6.143 0Zm10 0h-4.286A1.857 1.857 0 0 0 10 1.857v4.286C10 7.169 10.831 8 11.857 8h4.286A1.857 1.857 0 0 0 18 6.143V1.857A1.857 1.857 0 0 0 16.143 0Zm-10 10H1.857A1.857 1.857 0 0 0 0 11.857v4.286C0 17.169.831 18 1.857 18h4.286A1.857 1.857 0 0 0 8 16.143v-4.286A1.857 1.857 0 0 0 6.143 10Zm10 0h-4.286A1.857 1.857 0 0 0 10 11.857v4.286c0 1.026.831 1.857 1.857 1.857h4.286A1.857 1.857 0 0 0 18 16.143v-4.286A1.857 1.857 0 0 0 16.143 10Z"/>
        </svg>
      )
    },
    { 
      label: 'Dashboard', 
      view: 'dashboard', 
      icon: (
        <svg className="w-5 h-5 text-gray-500 transition duration-75 group-hover:text-green-600" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20">
          <path d="m17.418 3.623-.018-.008a6.713 6.713 0 0 0-2.4-.569V2h1a1 1 0 1 0 0-2h-2a1 1 0 0 0-1 1v2H9.89A6.977 6.977 0 0 1 12 8v5h-2V8A5 5 0 1 0 0 8v6a1 1 0 0 0 1 1h8v4a1 1 0 0 0 1 1h2a1 1 0 0 0 1-1v-4h6a1 1 0 0 0 1-1V8a5 5 0 0 0-2.582-4.377ZM6 12H4a1 1 0 0 1 0-2h2a1 1 0 0 1 0 2Z"/>
        </svg>
      )
    },
    { 
      label: 'Discover', 
      view: 'discover', 
      icon: (
        <svg className="w-5 h-5 text-gray-500 transition duration-75 group-hover:text-blue-600" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 18">
          <path d="M14 2a3.963 3.963 0 0 0-1.4.267 6.439 6.439 0 0 1-1.331 6.638A4 4 0 1 0 14 2Zm1 9h-1.264A6.957 6.957 0 0 1 15 15v2a2.97 2.97 0 0 1-.184 1H19a1 1 0 0 0 1-1v-1a5.006 5.006 0 0 0-5-5ZM6.5 9a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9ZM8 10H5a5.006 5.006 0 0 0-5 5v2a1 1 0 0 0 1 1h11a1 1 0 0 0 1-1v-2a5.006 5.006 0 0 0-5-5Z"/>
        </svg>
      )
    },
    { 
      label: 'Export', 
      view: 'export', 
      icon: (
        <svg className="w-5 h-5 text-gray-500 transition duration-75 group-hover:text-green-600" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 18 20">
          <path d="M17 5.923A1 1 0 0 0 16 5h-3V4a4 4 0 1 0-8 0v1H2a1 1 0 0 0-1 .923L.086 17.846A2 2 0 0 0 2.08 20h13.84a2 2 0 0 0 1.994-2.153L17 5.923ZM7 9a1 1 0 0 1-2 0V7h2v2Zm0-5a2 2 0 1 1 4 0v1H7V4Zm6 5a1 1 0 1 1-2 0V7h2v2Z"/>
        </svg>
      )
    }
  ];

  const modelFilterItems = [
    { label: 'HP LaserJet Pro M404dn', model: 'HP LaserJet Pro M404dn' },
    { label: 'HP LaserJet M406', model: 'HP LaserJet M406' },
    { label: 'ECOSYS P3145dn', model: 'ECOSYS P3145dn' },
    { label: 'HP LaserJet Pro M501dn', model: 'HP LaserJet Pro M501dn' },
  ];

  return (
    <>
      {/* Mobile Toggle Button */}
      <button 
        data-drawer-target="default-sidebar" 
        data-drawer-toggle="default-sidebar" 
        aria-controls="default-sidebar" 
        type="button" 
        className="inline-flex items-center p-2 mt-2 ms-3 text-sm text-gray-500 rounded-lg sm:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200"
        onClick={() => setIsOpen(!isOpen)}
      >
        <span className="sr-only">Open sidebar</span>
        <svg className="w-6 h-6" aria-hidden="true" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
          <path clipRule="evenodd" fillRule="evenodd" d="M2 4.75A.75.75 0 012.75 4h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 4.75zm0 10.5a.75.75 0 01.75-.75h7.5a.75.75 0 010 1.5h-7.5a.75.75 0 01-.75-.75zM2 10a.75.75 0 01.75-.75h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 10z"></path>
        </svg>
      </button>

      {/* Sidebar */}
      <aside 
        id="default-sidebar" 
        className={`fixed top-0 left-0 z-40 h-screen transition-all duration-300 ${isOpen ? 'translate-x-0' : '-translate-x-full'} sm:translate-x-0 ${isCollapsed ? 'w-16' : 'w-64'}`} 
        aria-label="Sidebar"
      >
        <div className="h-full px-3 py-4 overflow-y-auto bg-white border-r border-gray-200 shadow-lg">
          {/* Collapse Toggle Button */}
          <div className="flex justify-end mb-4">
            <button
              onClick={() => setIsCollapsed(!isCollapsed)}
              className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors duration-200"
              title={isCollapsed ? "Expand Sidebar" : "Collapse Sidebar"}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={isCollapsed ? "M13 5l7 7-7 7M5 5l7 7-7 7" : "M11 19l-7-7 7-7m8 14l-7-7 7-7"}></path>
              </svg>
            </button>
          </div>

          {/* Header */}
          <div className={`mb-6 text-center ${isCollapsed ? 'hidden' : 'block'}`}>
            <h2 className="text-2xl font-bold text-gray-800 mb-2">Navigation</h2>
            <p className="text-sm text-gray-600">Manage your printer fleet</p>
          </div>

          {/* Main Navigation */}
          <ul className="space-y-2 font-medium mb-6">
            {navigationItems.map((item) => (
              <li key={item.view}>
                <button
                  onClick={() => setView(item.view)}
                  className="flex items-center w-full p-2 text-gray-900 rounded-lg hover:bg-blue-50 hover:text-blue-700 group transition-colors duration-200 relative"
                  title={isCollapsed ? item.label : ""}
                >
                  {item.icon}
                  {!isCollapsed && <span className="ms-3">{item.label}</span>}
                  {/* Tooltip for collapsed state */}
                  {isCollapsed && (
                    <div className="absolute left-full ml-2 px-2 py-1 bg-gray-800 text-white text-sm rounded-md opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap z-50">
                      {item.label}
                    </div>
                  )}
                </button>
              </li>
            ))}
          </ul>

          {/* Model Filter Section */}
          <div className={`mb-6 ${isCollapsed ? 'hidden' : 'block'}`}>
            <h3 className="text-sm font-semibold text-gray-700 mb-3 text-center uppercase tracking-wider">Filter by Model</h3>
            <ul className="space-y-2">
              {modelFilterItems.map((item) => (
                <li key={item.model}>
                  <button
                    onClick={() => handleModelFilter(item.model)}
                    className="flex items-center w-full p-2 text-sm text-gray-600 rounded-lg hover:bg-green-50 hover:text-green-700 group transition-colors duration-200 relative"
                    title={isCollapsed ? item.label : ""}
                  >
                    <svg className="w-4 h-4 text-gray-500 group-hover:text-green-600" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10 0a10 10 0 1 0 10 10A10.009 10.009 0 0 0 10 0Zm3.982 13.982a1 1 0 0 1-1.414 0l-3.274-3.274A1.012 1.012 0 0 1 9 10V6a1 1 0 0 1 2 0v3.586l2.982 2.982a1 1 0 0 1 0 1.414Z"/>
                    </svg>
                    {!isCollapsed && <span className="ms-3 text-xs leading-tight">{item.label}</span>}
                    {/* Tooltip for collapsed state */}
                    {isCollapsed && (
                      <div className="absolute left-full ml-2 px-2 py-1 bg-gray-800 text-white text-xs rounded-md opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap z-50">
                        {item.label}
                      </div>
                    )}
                  </button>
                </li>
              ))}
            </ul>
          </div>

          {/* Footer */}
          <div className={`mt-auto text-center ${isCollapsed ? 'hidden' : 'block'}`}>
            <p className="text-xs text-gray-500 mb-2">Printer Management System</p>
            <div className="w-8 h-1 bg-gradient-to-r from-blue-400 to-green-500 rounded-full mx-auto"></div>
          </div>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;