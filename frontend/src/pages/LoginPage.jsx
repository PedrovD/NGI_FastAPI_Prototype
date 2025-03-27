import { useSearchParams, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { notification } from "../components/notifications/NotifySystem";
import { API_BASE_URL, login } from "../services";

export default function LoginPage() {
  const [searchParams] = useSearchParams();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    // Fetch available users
    fetch(`${API_BASE_URL}api/v1/users`, {
      method: "GET",
      credentials: "include",
      headers: {
        "Accept": "application/json",
        "Content-Type": "application/json"
      }
    })
      .then(response => {
        if (!response.ok) {
          throw new Error(`Failed to fetch users: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        console.log("Users data:", data);
        setUsers(data.users || []);
        setLoading(false);
      })
      .catch(error => {
        console.error("Error fetching users:", error);
        notification.error("Failed to load users. Please try again later.");
        setLoading(false);
        
        // Fallback: Create dummy users if API fails
        setUsers([
          { user_id: 1, username: "Student User", role: "student", image_path: "/default_profile_picture.png" },
          { user_id: 2, username: "Teacher User", role: "teacher", image_path: "/default_profile_picture.png" },
          { user_id: 3, username: "Supervisor User", role: "supervisor", image_path: "/default_profile_picture.png", business_id: 1 }
        ]);
      });
  }, []);

  const handleLogin = async (userId) => {
    try {
      // Use the login function from services.js
      await login(userId);
      
      // Redirect to home page after successful login
      navigate("/home");
    } catch (error) {
      console.error("Login error:", error);
      notification.error("Failed to login. Please try again.");
      
      // For testing, we'll still redirect to home page
      navigate("/");
    }
  };

  const error = searchParams.get("error");
  if (error !== null) {
    notification.error(error);
  }

  return (
    <div className="w-full min-h-dvh flex items-center justify-center p-4">
      <div className="w-sm p-4 sm:p-8 border border-gray-300 rounded-lg shadow-sm">
        <h1 className="mb-4 flex text-center justify-center items-center text-3xl sm:text-4xl font-bold">
          CMD Opdrachtenbox
        </h1>
        <h2 className="mb-4 text-center text-lg font-semibold">
          Kies een gebruiker om in te loggen
        </h2>
        
        {loading ? (
          <div className="text-center py-4">Loading users...</div>
        ) : (
          <div className="flex flex-col gap-3">
            {users.filter(user => user.role === "student").map(user => (
              <button
                key={user.user_id}
                onClick={() => handleLogin(user.user_id)}
                className="btn bg-blue-500 hover:bg-blue-600 text-white flex justify-center items-center gap-3"
              >
                <StudentIcon className="w-5 aspect-square" />
                <span className="text-lg font-semibold">Login als Student</span>
              </button>
            ))}
            
            {users.filter(user => user.role === "teacher").map(user => (
              <button
                key={user.user_id}
                onClick={() => handleLogin(user.user_id)}
                className="btn bg-green-500 hover:bg-green-600 text-white flex justify-center items-center gap-3"
              >
                <TeacherIcon className="w-5 aspect-square" />
                <span className="text-lg font-semibold">Login als Docent</span>
              </button>
            ))}
            
            {users.filter(user => user.role === "supervisor").map(user => (
              <button
                key={user.user_id}
                onClick={() => handleLogin(user.user_id)}
                className="btn bg-purple-500 hover:bg-purple-600 text-white flex justify-center items-center gap-3"
              >
                <SupervisorIcon className="w-5 aspect-square" />
                <span className="text-lg font-semibold">Login als Begeleider</span>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

const StudentIcon = ({ className }) => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className={className}>
    <path d="M12 3L1 9L5 11.18V17.18L12 21L19 17.18V11.18L21 10.09V17H23V9L12 3ZM18.82 9L12 12.72L5.18 9L12 5.28L18.82 9ZM17 15.99L12 18.72L7 15.99V12.27L12 15L17 12.27V15.99Z" />
  </svg>
)

const TeacherIcon = ({ className }) => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className={className}>
    <path d="M20 17H22V15H20V17ZM20 7V13H22V7H20ZM12 9V15H14V9H12ZM12 17H14V19H12V17ZM16 13H18V7H16V13ZM16 17H18V15H16V17ZM8 13H10V7H8V13ZM8 17H10V15H8V17ZM4 13H6V7H4V13ZM4 17H6V15H4V17Z" />
  </svg>
)

const SupervisorIcon = ({ className }) => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className={className}>
    <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM7.07 18.28C7.5 17.38 10.12 16.5 12 16.5C13.88 16.5 16.51 17.38 16.93 18.28C15.57 19.36 13.86 20 12 20C10.14 20 8.43 19.36 7.07 18.28ZM18.36 16.83C16.93 15.09 13.46 14.5 12 14.5C10.54 14.5 7.07 15.09 5.64 16.83C4.62 15.49 4 13.82 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 13.82 19.38 15.49 18.36 16.83ZM12 6C10.06 6 8.5 7.56 8.5 9.5C8.5 11.44 10.06 13 12 13C13.94 13 15.5 11.44 15.5 9.5C15.5 7.56 13.94 6 12 6ZM12 11C11.17 11 10.5 10.33 10.5 9.5C10.5 8.67 11.17 8 12 8C12.83 8 13.5 8.67 13.5 9.5C13.5 10.33 12.83 11 12 11Z" />
  </svg>
)
