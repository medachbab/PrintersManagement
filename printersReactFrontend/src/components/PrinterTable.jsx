import React from 'react';

const PrinterTable = ({ printers, onRefresh, onDelete }) => {
    return (
        <div className="bg-white rounded-2xl border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 hover:bg-gray-50 hover:border-gray-300 p-6">
            <h2 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent mb-6 text-center">
                📊 Printer Status Table
            </h2>
            
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">IP Address</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Model</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Serial Number</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Toner Level</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Page Count</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Last Refresh</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {Array.isArray(printers) && printers.map((printer) => (
                            <tr key={printer?.id || Math.random()} className="hover:bg-gray-50 transition-colors duration-200">
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm font-medium text-gray-900">
                                        {printer.name || 'Unnamed Printer'}
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-600 font-mono">
                                        {printer.ipAddress || 'No IP'}
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-600">
                                        {printer.model || 'Unknown Model'}
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-600 font-mono">
                                        {printer.serialNumber || 'No Serial'}
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="flex items-center">
                                        {printer.tonerLevel === -2 || printer.tonerLevel === null ? (
                                            <span className="text-sm font-semibold text-gray-600">
                                                Unknown
                                            </span>
                                        ) : (
                                            <>
                                                <div className="w-24 bg-gray-200 rounded-full h-3 mr-3">
                                                    <div
                                                        className="h-3 rounded-full transition-all duration-300"
                                                        style={{
                                                            width: `${printer.tonerLevel || 0}%`,
                                                            backgroundColor: (printer.tonerLevel || 0) > 20 ? '#10b981' : (printer.tonerLevel || 0) > 10 ? '#f59e0b' : '#ef4444',
                                                        }}
                                                    ></div>
                                                </div>
                                                <span className={`text-sm font-semibold ${
                                                    (printer.tonerLevel || 0) > 20 ? 'text-emerald-600' : 
                                                    (printer.tonerLevel || 0) > 10 ? 'text-orange-600' : 'text-red-600'
                                                }`}>
                                                    {printer.tonerLevel || 0}%
                                                </span>
                                            </>
                                        )}
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-600 font-mono">
                                        {printer.pageCount ? printer.pageCount.toLocaleString() : 'N/A'}
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-600">
                                        {printer.lastRefreshTime ? new Date(printer.lastRefreshTime).toLocaleString() : 'Never'}
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                    <div className="flex items-center gap-3">
                                        <button
                                            onClick={() => printer?.id && onRefresh ? onRefresh(printer.id) : null}
                                            className="relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 min-h-[44px] px-4 py-2 cursor-pointer bg-blue-600 text-white border-blue-500 shadow-blue-600/20 hover:bg-blue-500 hover:border-blue-400 hover:shadow-lg"
                                        >
                                            <span className="font-medium text-sm tracking-wide">🔄 Refresh</span>
                                        </button>
                                        <button
                                            onClick={() => printer?.id && onDelete ? onDelete(printer.id) : null}
                                            className="relative overflow-hidden transition-all duration-300 ease-out transform hover:-translate-y-1 active:translate-y-0 border border-transparent rounded-lg font-medium text-sm tracking-wide shadow-sm hover:shadow-md flex items-center justify-center gap-3 min-h-[44px] px-4 py-2 cursor-pointer bg-red-600 text-white border-red-500 shadow-red-600/20 hover:bg-red-500 hover:border-red-400 hover:shadow-lg"
                                        >
                                            <span className="font-medium text-sm tracking-wide">🗑️ Delete</span>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            
            {(!Array.isArray(printers) || printers.length === 0) && (
                <div className="text-center py-12">
                    <div className="w-20 h-20 bg-gray-100 rounded-full mx-auto mb-4 flex items-center justify-center">
                        <div className="text-4xl">🖨️</div>
                    </div>
                    <h3 className="text-lg font-medium text-gray-900 mb-2">No printers found</h3>
                    <p className="text-gray-600">Try adjusting your filters or discover new printers on your network.</p>
                </div>
            )}
        </div>
    );
};

export default PrinterTable;