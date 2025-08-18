import React, { useState, useEffect } from 'react';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalPrinters: 0,
    lowTonerPrinters: 0,
    highPageCountPrinters: 0
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
    <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-12 text-center min-h-[600px]">
      <h2 className="text-4xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent mb-6">
        📊 Printer Fleet Dashboard
      </h2>
      <p className="text-lg text-gray-600 max-w-2xl mx-auto mb-12">
        Real-time metrics, charts, and key performance indicators for your printers, including toner levels, page counts, and status.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Total Printers Card */}
        <div className="bg-white p-6 rounded-lg shadow-md border border-gray-100 transition-transform transform hover:scale-105">
          <div className="text-5xl font-extrabold text-gray-800">{stats.totalPrinters}</div>
          <div className="text-lg font-medium text-gray-500 mt-2">Total Printers</div>
        </div>

        {/* Low Toner Printers Card */}
        <div className="bg-white p-6 rounded-lg shadow-md border border-gray-100 transition-transform transform hover:scale-105">
          <div className="text-5xl font-extrabold text-red-600">{stats.lowTonerPrinters}</div>
          <div className="text-lg font-medium text-gray-500 mt-2">Printers with Low Toner</div>
        </div>

        {/* High Page Count Printers Card */}
        <div className="bg-white p-6 rounded-lg shadow-md border border-gray-100 transition-transform transform hover:scale-105">
          <div className="text-5xl font-extrabold text-yellow-600">{stats.highPageCountPrinters}</div>
          <div className="text-lg font-medium text-gray-500 mt-2">Printers with High Page Count</div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;