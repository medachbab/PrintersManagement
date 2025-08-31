const SemiCircularProgress = ({ value, max, title, color, icon, onClick }) => {
    const percentage = title === "Total Printers" ? 100 : (value / max) * 100;
    const radius = 45;
    const circumference = radius * Math.PI;
    const strokeDashoffset = circumference - (percentage / 100) * circumference;

    return (
      <div 
        onClick={onClick}
        className="text-center p-4 bg-gray-50 rounded-lg border border-gray-200 cursor-pointer 
                  transform transition-all duration-300 hover:scale-105 hover:shadow-lg"
      >
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
          <div className="absolute inset-x-0 bottom-0 flex flex-col items-center"> 
            
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
export default SemiCircularProgress;