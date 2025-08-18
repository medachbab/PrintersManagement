import React, { useState, useEffect } from 'react';

const DiscoverPrinters = ({ onDiscoveryComplete }) => {
  const [progress, setProgress] = useState(0);
  const [inProgress, setInProgress] = useState(false);
  const [subnetPrefix, setSubnetPrefix] = useState({ part1: '10', part2: '50', part3: '95' });
  const [discoveredCount, setDiscoveredCount] = useState(0);
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    let intervalId;
    if (inProgress) {
      intervalId = setInterval(async () => {
        try {
          const response = await fetch('http://localhost:8080/discoveryProgress');
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          const data = await response.json();
          setProgress(data.progress);
          setInProgress(data.inProgress);
          
          if (!data.inProgress) {
            clearInterval(intervalId);
            try {
              const printersResponse = await fetch('http://localhost:8080/printers');
              if (printersResponse.ok) {
                const printers = await printersResponse.json();
                setDiscoveredCount(printers.length);
                setShowSuccess(true);
                setTimeout(() => setShowSuccess(false), 5000);
              }
            } catch (error) {
              console.error("Failed to fetch printer count:", error);
            }
            onDiscoveryComplete();
          }
        } catch (error) {
          console.error("Failed to fetch discovery progress:", error);
          clearInterval(intervalId);
          setInProgress(false);
        }
      }, 2000);
    }
    return () => clearInterval(intervalId);
  }, [inProgress, onDiscoveryComplete]);

  const handleDiscover = async () => {
    try {
      setInProgress(true);
      setProgress(0);
      // NEW: Reset success state when starting new discovery
      setShowSuccess(false);
      setDiscoveredCount(0);
      await fetch(`http://localhost:8080/discoverPrinters?subnetPrefix=${subnetPrefix.part1}.${subnetPrefix.part2}.${subnetPrefix.part3}.`, {
        method: 'POST',
      });
    } catch (error) {
      console.error("Failed to start discovery:", error);
      setInProgress(false);
    }
  };

  const renderButtonText = () => {
    if (inProgress) {
      if (progress > 0 && progress < 100) return `${progress}%`;
      if (progress === 0) return 'Starting...';
    }
    return 'Discover';
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-8">
      <h2 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent mb-8 text-center">
        🔍 Discover Printers on Network
      </h2>
      
      {/* NEW: Success Message */}
      {showSuccess && (
        <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-center">
          <div className="flex items-center justify-center gap-2 text-green-700">
            <span className="text-lg font-semibold">
              Discovery Complete! Found {discoveredCount} printer{discoveredCount !== 1 ? 's' : ''} on your network.
            </span>
          </div>
        </div>
      )}
      
      <div className="flex flex-wrap items-end justify-center gap-8">
        {/* Subnet Configuration */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6 min-w-[300px]">
          <label className="block text-sm font-semibold text-gray-700 mb-4 text-center">
            🌐 Subnet Prefix Configuration
          </label>
          <div className="flex items-center justify-center space-x-2">
            <input
              type="number"
              value={subnetPrefix.part1}
              onChange={(e) => setSubnetPrefix({ ...subnetPrefix, part1: e.target.value })}
              className="w-16 p-3 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-center text-lg font-medium transition-all duration-200 text-gray-800"
              placeholder="1-255"
              min="1"
              max="255"
            />
            <span className="text-2xl text-gray-400">.</span>
            <input
              type="number"
              value={subnetPrefix.part2}
              onChange={(e) => setSubnetPrefix({ ...subnetPrefix, part2: e.target.value })}
              className="w-16 p-3 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-center text-lg font-medium transition-all duration-200 text-gray-800"
              placeholder="1-255"
              min="1"
              max="255"
            />
            <span className="text-2xl text-gray-400">.</span>
            <input
              type="number"
              value={subnetPrefix.part3}
              onChange={(e) => setSubnetPrefix({ ...subnetPrefix, part3: e.target.value })}
              className="w-16 p-3 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-center text-lg font-medium transition-all duration-200 text-gray-800"
              placeholder="1-255"
              min="1"
              max="255"
            />
            <span className="text-2xl text-gray-400">.*</span>
          </div>
          <p className="text-xs text-gray-500 mt-3 text-center">
            Example: 192.168.1.* will scan 192.168.1.1 to 192.168.1.254
          </p>
        </div>

        {/* Discovery Button */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6 flex flex-col items-center">
          <button
            onClick={handleDiscover}
            className="relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-lg tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 min-h-[44px] px-8 py-4 cursor-pointer bg-blue-600 text-white border-blue-500 shadow-blue-600/20 hover:bg-blue-500 hover:border-blue-400 hover:shadow-lg"
            disabled={inProgress}
          >
            <span className="font-medium text-lg tracking-wide">🔍 {renderButtonText()}</span>
            
            {/* Progress Overlay */}
            {inProgress && (
              <div
                className="absolute inset-0 rounded-xl progress-gradient z-0"
                style={{ '--progress': `${progress}%` }}
              ></div>
            )}
          </button>
          
          {/* Status Indicator */}
          {inProgress && (
            <div className="mt-4 text-center">
              <div className="w-full bg-gray-200 rounded-full h-2 mb-2">
                <div 
                  className="bg-gradient-to-r from-blue-600 to-blue-700 h-2 rounded-full transition-all duration-500"
                  style={{ width: `${progress}%` }}
                ></div>
              </div>
              <p className="text-sm text-gray-600">
                {progress === 0 ? 'Initializing...' : `Scanning network... ${progress}%`}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Instructions */}
      <div className="mt-12 text-center">
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6 max-w-2xl mx-auto">
          <h3 className="text-lg font-semibold text-gray-700 mb-3">📚 How Network Discovery Works</h3>
          <div className="text-sm text-gray-600 space-y-2">
            <p>• The system will scan the specified subnet for network devices</p>
            <p>• Printers are identified by their network responses and SNMP capabilities</p>
            <p>• Discovery typically takes 2-5 minutes depending on network size</p>
            <p>• Found printers will be automatically added to your inventory</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DiscoverPrinters;