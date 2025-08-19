import React, { useState, useEffect } from 'react';

const CircularProgress = ({ value, max, title, color, icon }) => {
  const percentage = title === "Total Printers" ? 100 : (value / max) * 100;
  const circumference = 2 * Math.PI * 45; // 45 is the radius
  const strokeDashoffset = circumference - (percentage / 100) * circumference;

  return (
    <div className="flex flex-col items-center p-6 bg-white rounded-xl shadow-lg">
      <div className="relative w-48 h-48">
        <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
          {/* Background circle */}
          <circle
            className="text-gray-200"
            strokeWidth="10"
            stroke="currentColor"
            fill="transparent"
            r="45"
            cx="50"
            cy="50"
          />
          {/* Progress circle */}
          <circle
            className={`${color} transition-all duration-300 ease-in-out`}
            strokeWidth="10"
            stroke="currentColor"
            fill="transparent"
            r="45"
            cx="50"
            cy="50"
            style={{
              strokeDasharray: circumference,
              strokeDashoffset: strokeDashoffset
            }}
          />
        </svg>
        {/* Center text */}
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-3xl font-bold">{value}</span>
          <span className="text-xl">{icon}</span>
        </div>
      </div>
      <div className="mt-4 text-center">
        <h3 className="text-xl font-semibold text-gray-700">{title}</h3>
        <p className="text-gray-600">out of {max}</p>
      </div>
    </div>
  );
};

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalPrinters: 0,
    lowTonerPrinters: 0,
    highPageCountPrinters: 0,
    unknownTonerLevelPrinters: 0 
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        const response = await fetch('http://localhost:8080/printers/dashboard-stats');
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        setStats(data);
        setError(null);
      } catch (e) {
        console.error("Failed to fetch dashboard stats:", e);
        setError("Failed to fetch data. Please ensure the backend is running and the endpoint is correct.");
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  if (loading) {
    return (
      <div className="text-center p-12">
        <p className="text-xl text-gray-500">Loading dashboard data...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center p-12 text-red-600">
        <p className="text-xl font-bold">Error:</p>
        <p>{error}</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-lg p-8">
      <h2 className="text-3xl font-bold text-center mb-12">
        📊 Printers Dashboard
      </h2>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
        <CircularProgress
          value={stats.totalPrinters}
          max={100}
          title="Total Printers"
          color="text-blue-500"
          icon="🖨️"
        />
        <CircularProgress
          value={stats.lowTonerPrinters}
          max={stats.totalPrinters}
          title="Low Toner Printers"
          color="text-red-500"
          icon="⚠️"
        />
        <CircularProgress
          value={stats.unknownTonerLevelPrinters}
          max={stats.totalPrinters}
          title="Printers with unknown toner level"
          color="text-red-500"
          icon="⚠️"
        />
        <CircularProgress
          value={stats.highPageCountPrinters}
          max={stats.totalPrinters}
          title="High Page Count"
          color="text-yellow-500"
          icon="📄"
        />
      </div>

      <div className="mt-12 p-6 bg-gray-50 rounded-lg">
        <h3 className="text-xl font-semibold mb-4">Quick Summary</h3>
        <ul className="list-disc list-inside space-y-2 text-gray-700">
          <li>{stats.totalPrinters} total printers in the network</li>
          <li>{stats.lowTonerPrinters} printers need toner replacement</li>
          <li>{stats.highPageCountPrinters} printers have high page counts</li>
        </ul>
      </div>
    </div>
  );
};

export default Dashboard;