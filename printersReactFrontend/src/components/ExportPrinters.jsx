import React, { useState } from 'react';

const ExportPrinters = () => {
  const [ipsToExport, setIpsToExport] = useState('');

  const handleExportAll = () => {
    window.location.href = 'http://localhost:8080/printers/download/excel';
  };

  const handleExportSelected = () => {
    const ipAddresses = ipsToExport.split(/[\n,]/).map(ip => ip.trim()).filter(ip => ip !== '');
    if (ipAddresses.length === 0) {
      alert('Please enter at least one IP address to export.');
      return;
    }
    const ipAddressesParam = ipAddresses.join(',');
    window.location.href = `http://localhost:8080/printers/download/excel/selected?ipAddresses=${ipAddressesParam}`;
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-8">
      <h2 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent mb-8 text-center">
        📤 Export Printers to Excel
      </h2>
      
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 max-w-6xl mx-auto">
        {/* Export All Section */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-8 text-center hover:scale-105 transition-transform duration-300">
          <div className="w-20 h-20 bg-blue-100 rounded-full mx-auto mb-6 flex items-center justify-center">
            <div className="text-4xl">📊</div>
          </div>
          <h3 className="text-2xl font-semibold text-gray-700 mb-4">Export All Printers</h3>
          <p className="text-gray-600 mb-6">
            Download a complete Excel report of all printers in your inventory with detailed information.
          </p>
          <button 
            onClick={handleExportAll} 
            className="relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-lg tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 min-h-[44px] px-8 py-4 cursor-pointer bg-blue-600 text-white border-blue-500 shadow-blue-600/20 hover:bg-blue-500 hover:border-blue-400 hover:shadow-lg"
          >
            <span className="font-medium text-lg tracking-wide">📥 Export All Printers</span>
          </button>
        </div>
        
        {/* Export Selected Section */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-8 hover:scale-105 transition-transform duration-300">
          <div className="w-20 h-20 bg-green-100 rounded-full mx-auto mb-6 flex items-center justify-center">
            <div className="text-4xl">🎯</div>
          </div>
          <h3 className="text-2xl font-semibold text-gray-700 mb-4">Export Selected Printers</h3>
          <p className="text-gray-600 mb-6">
            Export specific printers by entering their IP addresses below.
          </p>
          
          <div className="space-y-4">
            <div>
              <label htmlFor="ipAddressesToExport" className="block text-sm font-semibold text-gray-700 mb-2">
                🌐 IP Addresses (comma or newline separated):
              </label>
              <textarea
                id="ipAddressesToExport"
                rows="4"
                value={ipsToExport}
                onChange={(e) => setIpsToExport(e.target.value)}
                className="w-full p-4 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-gray-800 transition duration-200 resize-none"
                placeholder="e.g., 192.168.1.100, 192.168.1.101&#10;192.168.1.102"
              />
            </div>
            
            <button
              type="button"
              onClick={handleExportSelected}
              className="relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-lg tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 min-h-[44px] px-8 py-4 cursor-pointer bg-green-600 text-white border-green-500 shadow-green-600/20 hover:bg-green-500 hover:border-green-400 hover:shadow-lg w-full"
            >
              <span className="font-medium text-lg tracking-wide">📤 Export Selected</span>
            </button>
          </div>
        </div>
      </div>

      {/* Export Options Info */}
      <div className="mt-12 text-center">
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6 max-w-4xl mx-auto">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">✨ Export Features</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-sm text-gray-600">
            <div className="text-center">
              <div className="w-12 h-12 bg-blue-100 rounded-lg mx-auto mb-2 flex items-center justify-center">
                <div className="text-2xl">📋</div>
              </div>
              <p className="font-medium text-gray-700">Complete Data</p>
              <p>All printer details including status, toner levels, and page counts</p>
            </div>
            <div className="text-center">
              <div className="w-12 h-12 bg-green-100 rounded-lg mx-auto mb-2 flex items-center justify-center">
                <div className="text-2xl">⚡</div>
              </div>
              <p className="font-medium text-gray-700">Fast Processing</p>
              <p>Quick Excel generation with optimized formatting</p>
            </div>
            <div className="text-center">
              <div className="w-12 h-12 bg-purple-100 rounded-lg mx-auto mb-2 flex items-center justify-center">
                <div className="text-2xl">🔒</div>
              </div>
              <p className="font-medium text-gray-700">Secure Export</p>
              <p>Direct download with no data stored on external servers</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ExportPrinters;