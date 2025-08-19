import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import PrinterTable from './components/PrinterTable';
import Dashboard from './components/Dashboard';
import DiscoverPrinters from './components/DiscoverPrinters';
import ExportPrinters from './components/ExportPrinters';
import FilterSection from './components/FilterSection';

function App() {
  const SemiCircularProgress = ({ value, max, title, color, icon }) => {
  const percentage = title === "Total Printers" ? 100 : (value / max) * 100;
  const radius = 45;
  const circumference = radius * Math.PI;
  const strokeDashoffset = circumference - (percentage / 100) * circumference;

  return (
    <div className="text-center p-4 bg-gray-50 rounded-lg border border-gray-200">
      <div className="relative w-32 h-20 mx-auto">
        <svg className="w-full h-full" viewBox="0 0 100 50">
          {/* Background path */}
          <path
            className="text-gray-200"
            strokeWidth="10"
            stroke="currentColor"
            fill="transparent"
            d="M 10,50 A 40,40 0 1,1 90,50"
          />
          {/* Progress path */}
          <path
            className={`${color} transition-all duration-300 ease-in-out`}
            strokeWidth="10"
            strokeLinecap="round"
            stroke="currentColor"
            fill="transparent"
            d="M 10,50 A 40,40 0 1,1 90,50"
            style={{
              strokeDasharray: circumference,
              strokeDashoffset: strokeDashoffset
            }}
          />
        </svg>
        <div className="absolute inset-x-0 bottom-0 flex flex-col items-center"> {/* Added space-y-2 here */}
          
          <span className="text-2xl font-bold text-gray-800">{value}</span>
        </div>
      </div>
      <div className="mt-2">
        <div className="text-sm font-semibold text-gray-600">{title}</div>
        <span className="text-lg">{icon}</span>
      </div>
    </div>
  );
};
  const [printers, setPrinters] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [view, setView] = useState('welcome');
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [filters, setFilters] = useState({
    searchTerm: '',
    searchType: 'name',
    filterType: 'none',
    minToner: '',
    minPages: '',
  });

  const fetchPrinters = async () => {
    setLoading(true);
    setError(null);

    const params = new URLSearchParams();
    if (filters.searchTerm && filters.searchTerm !== '') params.append('searchTerm', filters.searchTerm);
    if (filters.searchType && filters.searchType !== 'name') params.append('searchType', filters.searchType);
    if (filters.filterType && filters.filterType !== 'none') params.append('filterType', filters.filterType);
    if (filters.minToner && filters.minToner !== '') params.append('minToner', filters.minToner);
    if (filters.minPages && filters.minPages !== '') params.append('minPages', filters.minPages);

    const queryString = params.toString();
    const url = `http://localhost:8080/printers?${queryString}`;

    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setPrinters(data);
    } catch (e) {
      setError("Failed to fetch printers. Please ensure the backend is running and CORS is configured.");
      console.error("Error fetching printers:", e);
    } finally {
      setLoading(false);
    }
  };
  const [stats, setStats] = useState({
  totalPrinters: 0,
  highPageCountPrinters: 0,
  lowTonerPrinters: 0
});
const [statsLoading, setStatsLoading] = useState(false);
const [statsError, setStatsError] = useState(null);

useEffect(() => {
  if (view === 'welcome') {
    const fetchDashboardStats = async () => {
      setStatsLoading(true);
      setStatsError(null);
      try {
        const response = await fetch('http://localhost:8080/printers/dashboard-stats');
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        setStats(data);
      } catch (e) {
        setStatsError("Failed to fetch dashboard stats.");
        console.error("Error fetching stats:", e);
      } finally {
        setStatsLoading(false);
      }
    };
    fetchDashboardStats();
  }
}, [view]);

  useEffect(() => {
    if (view === 'table') {
      fetchPrinters();
    }
  }, [filters, view]);

  const handleFilterChange = (newFilters) => {
    setFilters(prev => ({ ...prev, ...newFilters }));
  };

  const handleClearAllFilters = () => {
    setFilters({
      searchTerm: '',
      searchType: 'name',
      filterType: 'none',
      minToner: '',
      minPages: '',
    });
  };

  const handleModelFilter = (modelName) => {
    setFilters({
      searchTerm: modelName,
      searchType: 'model',
      filterType: 'none',
      minToner: '',
      minPages: '',
    });
    setView('table');
  };

  const handleRefreshPrinter = async (id) => {
    try {
      await fetch(`http://localhost:8080/printers/refresh/${id}`, { method: 'POST' });
      fetchPrinters();
    } catch (e) {
      console.error("Failed to refresh printer:", e);
    }
  };

  const handleDeletePrinter = async (id) => {
    if (window.confirm("Are you sure you want to delete this printer?")) {
      try {
        await fetch(`http://localhost:8080/printers/delete/${id}`, { method: 'DELETE' });
        alert(`Printer with ID ${id} deleted successfully.`);
        fetchPrinters();
      } catch (error) {
        console.error(`Error deleting printer ${id}:`, error);
        alert('Failed to delete printer.');
      }
    }
  };
  
  const renderContent = () => {
    switch (view) {
      case 'welcome':
        return (
          <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-12 text-center min-h-[600px]">

            {/* Main Content Grid */}
            <div className="flex flex-col space-y-8">
              {/* System Info - Now Horizontal */}
              <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6">
                <h3 className="text-lg font-semibold text-gray-700 mb-4 uppercase tracking-wider">System Status</h3>
                <div>
                  {statsLoading ? (
                    <p>Loading stats...</p>
                  ) : statsError ? (
                    <p className="text-red-500">{statsError}</p>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                      <SemiCircularProgress
                        value={stats.totalPrinters}
                        max={100}
                        title="Total Printers"
                        color="text-blue-500"
                        icon="🖨️"
                      />
                      <SemiCircularProgress
                        value={stats.highPageCountPrinters}
                        max={stats.totalPrinters}
                        title="High Page Count"
                        color="text-yellow-500"
                        icon="📄"
                      />
                      <SemiCircularProgress
                        value={stats.lowTonerPrinters}
                        max={stats.totalPrinters}
                        title="Low Toner"
                        color="text-red-500"
                        icon="⚠️"
                      />
                      <SemiCircularProgress
                        value={stats.unknownTonerLevelPrinters}
                        max={stats.totalPrinters}
                        title="Printers with unknown toner level"
                        color="text-red-500"
                        icon="⚠️"
                      />
                    </div>
                  )}
                </div>
              </div>

              {/* Navigation Grid - Below System Info */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 w-full max-w-7xl mx-auto">
                {/* Main Navigation Cards */}
                <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6 h-full flex flex-col">
                  <h3 className="text-lg font-semibold text-gray-700 mb-4 uppercase tracking-wider">Main Menu</h3>
                  <div className="flex-1 flex flex-col justify-between space-y-3">
                    <button
                      onClick={() => setView('table')}
                      className="w-full h-[60px] relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 px-5 bg-blue-600 text-white border-blue-500 shadow-blue-600/20 hover:bg-blue-500 hover:border-blue-400 hover:shadow-lg"
                    >
                      <span className="font-medium text-sm tracking-wide">🖨️ All Printers</span>
                    </button>
                    <button
                      onClick={() => setView('dashboard')}
                      className="w-full h-[60px] relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 px-5 bg-green-600 text-white border-green-500 shadow-green-600/20 hover:bg-green-500 hover:border-green-400 hover:shadow-lg"
                    >
                      <span className="font-medium text-sm tracking-wide">📊 Dashboard</span>
                    </button>
                    <button
                      onClick={() => setView('discover')}
                      className="w-full h-[60px] relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 px-5 bg-blue-600 text-white border-blue-500 shadow-blue-600/20 hover:bg-blue-500 hover:border-blue-400 hover:shadow-lg"
                    >
                      <span className="font-medium text-sm tracking-wide">🔍 Discover</span>
                    </button>
                    <button
                      onClick={() => setView('export')}
                      className="w-full h-[60px] relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 px-5 bg-green-600 text-white border-green-500 shadow-green-600/20 hover:bg-green-500 hover:border-green-400 hover:shadow-lg"
                    >
                      <span className="font-medium text-sm tracking-wide">📤 Export</span>
                    </button>
                  </div>
                </div>

                {/* Quick Model Filter */}
                <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6 h-full flex flex-col">
                  <h3 className="text-lg font-semibold text-gray-700 mb-4 uppercase tracking-wider">Quick Filters</h3>
                  <div className="flex-1 flex flex-col justify-between space-y-3">
                    <button
                      onClick={() => handleModelFilter('HP LaserJet Pro M404dn')}
                      className="w-full h-[60px] relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 px-5 bg-gray-600 text-white border-gray-500 shadow-gray-600/20 hover:bg-gray-500 hover:border-gray-400 hover:shadow-lg"
                    >
                      <span className="font-medium text-sm tracking-wide">🖨️ HP LaserJet Pro M404dn</span>
                    </button>
                    <button
                      onClick={() => handleModelFilter('HP LaserJet M406')}
                      className="w-full h-[60px] relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 px-5 bg-gray-600 text-white border-gray-500 shadow-gray-600/20 hover:bg-gray-500 hover:border-gray-400 hover:shadow-lg"
                    >
                      <span className="font-medium text-sm tracking-wide">🖨️ HP LaserJet M406</span>
                    </button>
                    <button
                      onClick={() => handleModelFilter('ECOSYS P3145dn')}
                      className="w-full h-[60px] relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 px-5 bg-gray-600 text-white border-gray-500 shadow-gray-600/20 hover:bg-gray-500 hover:border-gray-400 hover:shadow-lg"
                    >
                      <span className="font-medium text-sm tracking-wide">🖨️ ECOSYS P3145dn</span>
                    </button>
                    <button
                      onClick={() => handleModelFilter('HP LaserJet Pro M501dn')}
                      className="w-full h-[60px] relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 px-5 bg-gray-600 text-white border-gray-500 shadow-gray-600/20 hover:bg-gray-500 hover:border-gray-400 hover:shadow-lg"
                    >
                      <span className="font-medium text-sm tracking-wide">🖨️ HP LaserJet Pro M501dn</span>
                    </button>
                  </div>
                </div>
              </div>

              {/* Bottom CTA */}
              <div className="mt-12 text-center">
                <button
                  onClick={() => setView('table')}
                  className="relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-lg tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 min-h-[44px] px-8 py-4 cursor-pointer bg-blue-800 text-white border-blue-700 shadow-blue-800/20 hover:bg-blue-700 hover:border-blue-600 hover:shadow-lg"
                >
                  <span className="font-medium text-lg tracking-wide">🚀 View All Printers</span>
                </button>
              </div>
            </div>
          </div>
        );
      case 'table':
        return (
          <>
            <FilterSection filters={filters} onFilterChange={handleFilterChange} onClearAll={handleClearAllFilters} />
            {loading && <p className="text-center text-primary-blue my-4">Loading printers...</p>}
            {error && <p className="text-center text-red-500 my-4">{error}</p>}
            {!loading && !error && <PrinterTable printers={printers} onRefresh={handleRefreshPrinter} onDelete={handleDeletePrinter} />}
          </>
        );
      case 'dashboard':
        return <Dashboard />;
      case 'discover':
        return <DiscoverPrinters onDiscoveryComplete={fetchPrinters} />;
      case 'export':
        return <ExportPrinters />;
      default:
        return null;
    }
  };

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar 
        setView={setView} 
        handleModelFilter={handleModelFilter} 
        onCollapseChange={setSidebarCollapsed}
      />
      <div className={`flex-1 p-4 transition-all duration-300 ${sidebarCollapsed ? 'sm:ml-16' : 'sm:ml-64'}`}>
        <Header />
        {renderContent()}
      </div>
    </div>
  );
}

export default App;