const Loader = ({ size = 'md', text = 'Loading...' }) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  return (
    <div className="flex flex-col items-center justify-center py-8">
      <div className="relative">
        <div className={`${sizeClasses[size]} border-4 border-gray-300 border-t-indigo-600 rounded-full animate-spin`}></div>
      </div>
      {text && (
        <p className="mt-4 text-gray-400 text-sm">{text}</p>
      )}
    </div>
  );
};

export default Loader;

