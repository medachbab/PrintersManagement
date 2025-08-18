import React, { useState, useEffect } from 'react';

const FilterSection = ({ filters, onFilterChange, onClearAll }) => {
  const [showToner, setShowToner] = useState(false);
  const [showPages, setShowPages] = useState(false);

  useEffect(() => {
    const filterType = filters.filterType;
    setShowToner(filterType === 'none');
    setShowPages(filterType === 'none');
  }, [filters.filterType]);

  const toggleClearButton = (id) => {
    const input = document.getElementById(id);
    const clearButton = document.getElementById('clear' + id.charAt(0).toUpperCase() + id.slice(1));
    if (input && clearButton) {
      clearButton.style.display = input.value ? 'block' : 'none';
    }
  };

  const clearInput = (id) => {
    onFilterChange({ [id]: '' });
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-8 mb-8">
      <h3 className="text-2xl font-bold text-gray-700 mb-6 text-center">🔍 Filter & Search Printers</h3>
      
      <form onSubmit={(e) => e.preventDefault()} className="space-y-6">
        {/* Search Input */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6">
          <label htmlFor="searchTerm" className="block text-sm font-semibold text-gray-700 mb-3">🔎 Search Printers:</label>
          <div className="flex items-end gap-3">
            <div className="relative inline-block">
              <select
                id="searchType"
                name="searchType"
                value={filters.searchType}
                onChange={(e) => onFilterChange({ searchType: e.target.value })}
                className="block w-fit p-3 border border-gray-300 rounded-l-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-gray-800 transition duration-200 font-medium"
              >
                <option value="name">Name</option>
                <option value="ipAddress">IP Address</option>
                <option value="serialNumber">Serial Number</option>
                <option value="manufacturer">Manufacturer</option>
                <option value="model">Model</option>
              </select>
              <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
                <span className="text-xs text-gray-500">▼</span>
              </div>
            </div>
            <div className="relative flex-grow">
              <input
                type="text"
                id="searchTerm"
                name="searchTerm"
                value={filters.searchTerm}
                onChange={(e) => onFilterChange({ searchTerm: e.target.value })}
                onInput={() => toggleClearButton('searchTerm')}
                placeholder="Search by Name, IP, Serial No., Manufacturer or Model"
                className="w-full p-3 border border-gray-300 rounded-r-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-gray-800 transition duration-200"
              />
              <button
                type="button"
                id="clearSearch"
                onClick={() => clearInput('searchTerm')}
                className="absolute right-2 top-1/2 transform -translate-y-1/2 p-2 bg-gray-200 rounded-md text-gray-600 hover:bg-gray-300 transition duration-200"
                style={{ display: filters.searchTerm ? 'block' : 'none' }}
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>

        {/* Quick Status Filter */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6">
          <label htmlFor="filterType" className="block text-sm font-semibold text-gray-700 mb-3">⚡ Quick Status Filter:</label>
          <div className="relative inline-block w-full">
            <select
              id="filterType"
              name="filterType"
              value={filters.filterType}
              onChange={(e) => onFilterChange({ filterType: e.target.value, minToner: '', minPages: '' })}
              className="block w-full p-3 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-gray-800 transition duration-200 font-medium"
            >
              <option value="none">No Filter</option>
              <option value="tonerLow">Toner &lt; 20%</option>
              <option value="tonerMedium">Toner 20-50%</option>
              <option value="tonerHigh">Toner &gt; 50%</option>
              <option value="pagesLow">Pages &lt; 10k</option>
              <option value="pagesMedium">Pages 10k-50k</option>
              <option value="pagesHigh">Pages &gt; 50k</option>
              <option value="lastRefresh24h">Last Refresh &lt; 24h</option>
              <option value="lastRefresh7d">Last Refresh &lt; 7 days</option>
              <option value="lastRefresh30d">Last Refresh &lt; 30 days</option>
            </select>
            <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
              <span className="text-xs text-gray-500">▼</span>
            </div>
          </div>
        </div>

        {/* Custom Filters */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Min Toner Filter */}
          <div id="tonerFilterGroup" className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6" style={{ display: showToner ? 'block' : 'none' }}>
            <label htmlFor="minToner" className="block text-sm font-semibold text-gray-700 mb-3">🎨 Minimum Toner Level (%):</label>
            <div className="relative">
              <input
                type="number"
                id="minToner"
                name="minToner"
                value={filters.minToner}
                onChange={(e) => onFilterChange({ minToner: e.target.value })}
                onInput={() => toggleClearButton('minToner')}
                className="w-full p-3 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-gray-800 transition duration-200"
                min="0"
                max="100"
                placeholder="e.g., 20"
              />
              <button
                type="button"
                id="clearMinToner"
                onClick={() => clearInput('minToner')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 p-1 bg-gray-200 rounded-md text-gray-600 hover:bg-gray-300 transition duration-200"
                style={{ display: filters.minToner ? 'block' : 'none' }}
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>
          </div>

          {/* Min Pages Filter */}
          <div id="pagesFilterGroup" className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6" style={{ display: showPages ? 'block' : 'none' }}>
            <label htmlFor="minPages" className="block text-sm font-semibold text-gray-700 mb-3">📄 Minimum Page Count:</label>
            <div className="relative">
              <input
                type="number"
                id="minPages"
                name="minPages"
                value={filters.minPages}
                onChange={(e) => onFilterChange({ minPages: e.target.value })}
                onInput={() => toggleClearButton('minPages')}
                className="w-full p-3 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-gray-800 transition duration-200"
                min="0"
                placeholder="e.g., 10000"
              />
              <button
                type="button"
                id="clearMinPages"
                onClick={() => clearInput('minPages')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 p-1 bg-gray-200 rounded-md text-gray-600 hover:bg-gray-300 transition duration-200"
                style={{ display: filters.minPages ? 'block' : 'none' }}
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <button
            type="submit"
            className="relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-lg tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 min-h-[44px] px-8 py-4 cursor-pointer bg-blue-600 text-white border-blue-500 shadow-blue-600/20 hover:bg-blue-500 hover:border-blue-400 hover:shadow-lg"
          >
            <span className="font-medium text-lg tracking-wide">✅ Apply Filters</span>
          </button>
          <button
            type="button"
            onClick={onClearAll}
            className="relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-lg tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 min-h-[44px] px-8 py-4 cursor-pointer bg-red-600 text-white border-red-500 shadow-red-600/20 hover:bg-red-500 hover:border-red-400 hover:shadow-lg"
          >
            <span className="font-medium text-lg tracking-wide">🧹 Clear All Filters</span>
          </button>
        </div>
      </form>
    </div>
  );
};

export default FilterSection;